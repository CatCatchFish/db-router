package cn.cat.middleware.dbrouter;

public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTBKey();
    }

}
