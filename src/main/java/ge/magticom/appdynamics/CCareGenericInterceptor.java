package ge.magticom.appdynamics;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.SDKStringMatchType;
import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import java.util.*;

public class CCareGenericInterceptor extends AGenericInterceptor {
    private static final String CLASS_TO_INSTRUMENT = "com.magti.billing.ejb.beans.fascade.service.ws.portal.PortalCommonManagement";
    private static final String METHOD_TO_INSTRUMENT = "getPackagesCor";
    private IReflector ireflector_synH;

    public CCareGenericInterceptor() {
        super();
        this.ireflector_synH = getNewReflectionBuilder()
                .invokeInstanceMethod(METHOD_TO_INSTRUMENT, false, new String[]{"java.lang.String"} )
                .build();

    }

    @Override
    public List<Rule> initializeRules() {
        System.out.println("CCareGenericInterceptor In initializeRules========================================");
        Rule.Builder bldr = new Rule.Builder(CLASS_TO_INSTRUMENT);
        bldr = bldr.classMatchType(SDKClassMatchType.MATCHES_CLASS).classStringMatchType(SDKStringMatchType.EQUALS);
        bldr = bldr.methodMatchString(METHOD_TO_INSTRUMENT).methodStringMatchType(SDKStringMatchType.EQUALS);
        List<Rule> result = new ArrayList<>();
        result.add(bldr.build());
        return result;
    }

    public Object onMethodBegin(Object invokedObject, String className, String methodName, Object[] paramValues) {
        String singularityH = null;
        String requestURI = null;
        System.out.println("CCareGenericInterceptor onMethodBegin========================================"+className+" "+methodName+" " );
        System.out.println("CCareGenericInterceptor onMethodBegin========================================"+className+" "+methodName+" "+paramValues[0] );
        if(paramValues!=null && paramValues.length>0){
            for(int i=0;i<paramValues.length;i++){
                System.out.println("paramValues"+paramValues[i].toString());
            }
        }
        singularityH = paramValues[0].toString() ;
        System.out.println("CCareGenericInterceptor In begin isdk==================== singularityH=" + singularityH);
//        AppdynamicsAgent.startTransaction("SampleGenericInterceptorBT-" + singularityH, singularityH, EntryTypes.POJO, false);
        return null;
    }

    public void onMethodEnd(Object state, Object invokedObject, String className, String methodName,
                            Object[] paramValues, Throwable thrownException, Object returnValue) {
        System.out.println("In end");
//        Transaction currentTransaction = AppdynamicsAgent.getTransaction();
//        currentTransaction.end();
    }

}
