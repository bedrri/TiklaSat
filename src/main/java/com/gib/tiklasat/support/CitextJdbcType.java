package com.gib.tiklasat.support;

/** users.email, login_attempts.email gibi CITEXT sütunlar için (BR-U-001). */
public class CitextJdbcType extends PgTextJdbcType {
    public CitextJdbcType() {
        super("citext");
    }
}
