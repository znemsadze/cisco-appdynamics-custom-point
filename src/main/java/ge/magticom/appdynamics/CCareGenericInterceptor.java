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
import org.apache.log4j.Logger;
import java.util.*;

public class CCareGenericInterceptor extends AGenericInterceptor {
    private static final String CLASS_TO_INSTRUMENT = "com.magti.billing.ejb.beans.fascade.service.ws.portal";
    private static final String METHOD_TO_INSTRUMENT = "createOrderParent";
    private IReflector ireflector_synH;
    private IReflector ireflector_rURI;

    public CCareGenericInterceptor() {
        super();
        this.ireflector_synH = getNewReflectionBuilder()
                .invokeInstanceMethod("getRequestHeaders", false)
                .invokeInstanceMethod("getFirst", false, new String[]{"java.lang.String"} )
                .build();
        this.ireflector_rURI = getNewReflectionBuilder()
                .invokeInstanceMethod("getRequestURI", false)
                //.invokeInstanceMethod("getFirst", false, new String[]{"java.lang.String"} )
                .build();
    }

    @Override
    public List<Rule> initializeRules() {
        System.out.println("In initializeRules");
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
        try {
            singularityH = this.ireflector_synH.execute(paramValues[0].getClass().getClassLoader(),paramValues[0]
                    ,new Object[] {}, new String[] {UniqueIdentifiersEnum.CREATE_PORTAL_ORDER_IDENTIFIER.getValues()}
            );
            requestURI = this.ireflector_rURI.execute(paramValues[0].getClass().getClassLoader(),paramValues[0]
            ).toString();
            System.out.println("In begin isdk " + singularityH);
            System.out.println("In begin isdk " + requestURI);
        } catch (ReflectorException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        AppdynamicsAgent.startTransaction("SampleGenericInterceptorBT-" + requestURI, singularityH, EntryTypes.POJO, false);
        return null;
    }

    public void onMethodEnd(Object state, Object invokedObject, String className, String methodName,
                            Object[] paramValues, Throwable thrownException, Object returnValue) {
        System.out.println("In end");
        Transaction currentTransaction = AppdynamicsAgent.getTransaction();
        currentTransaction.end();
    }

}
