package cn.cat.middleware.dbrouter.type;

public enum PoolType {

    HIKARI("hikari"),
    C3P0("c3p0"),
    DRUID("druid"),
    DEFAULT("default");
    private final String value;

    PoolType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PoolType getPoolType(String value) {
        for (PoolType poolType : PoolType.values()) {
            if (poolType.getValue().equals(value)) {
                return poolType;
            }
        }
        return null;
    }

}
