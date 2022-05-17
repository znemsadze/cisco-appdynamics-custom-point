package ge.magticom.appdynamics;

public enum UniqueIdentifiersEnum {

    CREATE_PORTAL_ORDER_IDENTIFIER("PORTAL_ORDER_IDENTIFIER");

    private String value;

    UniqueIdentifiersEnum(String value) {
        this.value = value;
    }

    public String getValues(){
        return value;
    }
}
