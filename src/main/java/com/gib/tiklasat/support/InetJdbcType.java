package com.gib.tiklasat.support;

/** ip_address, consent_ip gibi INET sütunlar için. */
public class InetJdbcType extends PgTextJdbcType {
    public InetJdbcType() {
        super("inet");
    }
}
