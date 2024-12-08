package cn.cat.middleware.dbrouter;

@Deprecated
public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTBKey();
    }

}
