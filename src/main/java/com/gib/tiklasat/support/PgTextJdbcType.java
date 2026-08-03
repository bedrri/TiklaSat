package com.gib.tiklasat.support;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL'e özgü, standart JDBC'de karşılığı olmayan metin tabanlı tipler
 * (citext, inet gibi) için köprü.
 *
 * Sorun: bu sütunlar şema doğrulamasında (ddl-auto=validate) Types.OTHER
 * olarak görünüyor, ama alan düz String olarak bırakılırsa (veya salt
 * @JdbcTypeCode(SqlTypes.OTHER) ile işaretlenirse) PostgreSQL sürücüsü
 * parametreyi bytea'ya çevirip "citext = bytea" / "inet ama varchar geldi"
 * gibi hatalar üretiyor. Çözüm: değeri PGobject'e sarıp tipini açıkça
 * belirterek (setType) yazmak — sürücü artık doğru tipi biliyor.
 */
public class PgTextJdbcType implements JdbcType {

    private final String pgTypeName;

    protected PgTextJdbcType(String pgTypeName) {
        this.pgTypeName = pgTypeName;
    }

    @Override
    public int getJdbcTypeCode() {
        return Types.OTHER;
    }

    @Override
    public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
        return new BasicBinder<>(javaType, this) {
            @Override
            protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                st.setObject(index, toPgObject(javaType.unwrap(value, String.class, options)));
            }

            @Override
            protected void doBind(CallableStatement st, X value, String name, WrapperOptions options) throws SQLException {
                st.setObject(name, toPgObject(javaType.unwrap(value, String.class, options)));
            }
        };
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
        return new BasicExtractor<>(javaType, this) {
            @Override
            protected X doExtract(ResultSet rs, int position, WrapperOptions options) throws SQLException {
                return javaType.wrap(rs.getString(position), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                return javaType.wrap(statement.getString(index), options);
            }

            @Override
            protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                return javaType.wrap(statement.getString(name), options);
            }
        };
    }

    private PGobject toPgObject(String value) throws SQLException {
        PGobject obj = new PGobject();
        obj.setType(pgTypeName);
        obj.setValue(value);
        return obj;
    }
}
