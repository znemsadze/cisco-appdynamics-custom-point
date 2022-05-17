package ge.magticom.appdynamics;

import com.appdynamics.agent.api.*;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.SDKStringMatchType;
import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MymagtiInterceptor extends AGenericInterceptor {
    private static final String CLASS_TO_INSTRUMENT = "com.mymagti.core.services.impl";
    private static final String METHOD_TO_INSTRUMENT = "createOrderBase";
    public static Logger logger=Logger.getLogger(MymagtiInterceptor.class);
    @Override
    public List<Rule> initializeRules() {
        logger.info("MymagtiInterceptor initializeRules");
        Rule.Builder bldr = new Rule.Builder(CLASS_TO_INSTRUMENT);
        bldr = bldr.classMatchType(SDKClassMatchType.MATCHES_CLASS).classStringMatchType(SDKStringMatchType.EQUALS);
        bldr = bldr.methodMatchString(METHOD_TO_INSTRUMENT).methodStringMatchType(SDKStringMatchType.EQUALS);
        List<Rule> result = new ArrayList<Rule>();
        result.add(bldr.build());
        return result;
    }

    @Override
    public Object onMethodBegin(Object invokedObject, String className, String methodName, Object[] paramValues) {
        logger.info(className+" "+methodName+" onMethodBegin start");
        Transaction currentTransaction = AppdynamicsAgent.getTransaction();
        ExitCall exitCall = currentTransaction.startExitCall(UniqueIdentifiersEnum.CREATE_PORTAL_ORDER_IDENTIFIER.getValues(),
                String.format("iSDK  %s exit call", UniqueIdentifiersEnum.CREATE_PORTAL_ORDER_IDENTIFIER.getValues()),
                ExitTypes.CUSTOM, false);
        String correlationHeader = exitCall.getCorrelationHeader();
        logger.info("correlationHeader="+correlationHeader);
        String[] types = new String[]{String.class.getCanonicalName()};
        IReflector headerReflector = getNewReflectionBuilder()
                .invokeInstanceMethod("setHeader", false, types)
                .build();
        try {
            headerReflector.execute(paramValues[0].getClass().getClassLoader(),
                    paramValues[0], new Object[]{correlationHeader});
        } catch (ReflectorException e) {
            logger.error("Caught reflector exception", e);
        }
        return null;
    }

    @Override
    public void onMethodEnd(Object state, Object invokedObject, String className, String methodName, Object[] paramValues, Throwable thrownException, Object returnValue) {
            logger.info(className+" "+methodName+" method End");
    }


}
