package ge.magticom.appdynamics;

import com.appdynamics.agent.api.*;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.SDKStringMatchType;
import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import java.util.ArrayList;
import java.util.List;

public class MymagtiInterceptor extends AGenericInterceptor {
    private static final String CLASS_TO_INSTRUMENT = "com.mymagti.core.services.impl.OrderServiceimpl";
    private static final String METHOD_TO_INSTRUMENT = "createOrderBase";
    @Override
    public List<Rule> initializeRules() {
        System.out.println("MymagtiInterceptor initializeRules");
        System.out.println("Pittaloooooooo=========================================MymagtiInterceptor initializeRules");
        Rule.Builder bldr = new Rule.Builder(CLASS_TO_INSTRUMENT);
        bldr = bldr.classMatchType(SDKClassMatchType.MATCHES_CLASS).classStringMatchType(SDKStringMatchType.EQUALS);
        bldr = bldr.methodMatchString(METHOD_TO_INSTRUMENT).methodStringMatchType(SDKStringMatchType.EQUALS);
        List<Rule> result = new ArrayList<>();
        result.add(bldr.build());
        return result;
    }

    @Override
    public Object onMethodBegin(Object invokedObject, String className, String methodName, Object[] paramValues) {
        System.out.println(className+" "+methodName+" onMethodBegin start");
        System.out.printf("Pittaloooooooo========================================="+className+" "+methodName+" onMethodBegin start");
        Transaction currentTransaction = AppdynamicsAgent.getTransaction();
        ExitCall exitCall = currentTransaction.startExitCall( "exitCall",
                "iSDK  exit call" ,    ExitTypes.CUSTOM, false);
        String correlationHeader = exitCall.getCorrelationHeader();
        System.out.println("correlationHeader===================================="+correlationHeader);
        String[] types = new String[]{String.class.getCanonicalName()};
        IReflector headerReflector = getNewReflectionBuilder()
                .invokeInstanceMethod(METHOD_TO_INSTRUMENT, false, types)
                .build();
        try {
            headerReflector.execute(paramValues[22].getClass().getClassLoader(),
                    paramValues[22], new Object[]{correlationHeader});
        } catch (ReflectorException e) {
            System.out.println("Caught reflector exception=========================="+ e.getMessage());
        }
        return null;
    }

    @Override
    public void onMethodEnd(Object state, Object invokedObject, String className, String methodName, Object[] paramValues, Throwable thrownException, Object returnValue) {
        System.out.println(className+" "+methodName+" method End");
    }


}
