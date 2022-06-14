package ge.magticom.appdynamics;

import com.appdynamics.agent.api.*;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.SDKStringMatchType;
import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MymagtiInterceptor extends AGenericInterceptor {
    private static final String CLASS_TO_INSTRUMENT = "com.mymagti.core.services.impl.PackageServiceImpl";
    private static final String METHOD_TO_INSTRUMENT = "getPackagesBaseCor";


    private IReflector getErrorCode, getErrorMessage; //ResultObject

    public MymagtiInterceptor() {
        super();
        getErrorCode = getNewReflectionBuilder().invokeInstanceMethod("getErrorCode", true).build(); //if it doesn't return com.mymagti.core.constants.Constants.STATUS_OK, it is an error
        getErrorMessage = getNewReflectionBuilder().invokeInstanceMethod("getErrorMessage", true).build(); //i'm speculating here, please set this to something correct
//        getLogger().info(String.format("Initialized Magti Interceptor SDK Plugin")); //look in the agent log files for appd, debug is silenced unless pulling debug logs from controller
    }

    @Override
    public List<Rule> initializeRules() {
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule.Builder("com.mymagti.core.services.impl.PackageServiceImpl")
                .classMatchType(SDKClassMatchType.MATCHES_CLASS)
                .methodMatchString("getPackagesBaseCor") //producer
                .methodStringMatchType(SDKStringMatchType.EQUALS)
                .build()
        );
        rules.add(new Rule.Builder("com.magti.billing.ejb.beans.fascade.service.ws.portal.PortalCommonManagement")
                .classMatchType(SDKClassMatchType.MATCHES_CLASS)
                .methodMatchString("getPackagesCor") //consumer
                .methodStringMatchType(SDKStringMatchType.EQUALS)
                .build()
        );
        return rules;
    }

    @Override
    public Object onMethodBegin(Object invokedObject, String className, String methodName, Object[] paramValues) {
        String correlationHeader = null;
        if( paramValues.length > 0 ) correlationHeader = (String) paramValues[0];
        String typeId = "UNKNOWN-TYPEID";
        if( paramValues[3] != null ) typeId= String.valueOf(paramValues[3]);
        String contractTypeId = "UNKNOWN-CONTRACTTYPEID";
        if( paramValues[2] != null ) contractTypeId= String.valueOf(paramValues[2]);
        getLogger().info(String.format("onMethodBegin called for %s.%s( %s, ...)", className, methodName, correlationHeader)); //change to debug once you are happy with this working
        System.out.println(String.format("appdynamics================== onMethodBegin called for %s.%s( %s, ...)", className, methodName, correlationHeader)); //change to debug once you are happy with this working
        Transaction transaction = null;
        ExitCall exitCall = null;
        switch (methodName) {
            case "getPackagesBaseCor": {// producer, create exit call
                transaction = AppdynamicsAgent.getTransaction();
                if( "".equals(transaction.getUniqueIdentifier()) ) { //transaction is not real, log it, return null
                    getLogger().info("Transaction is not started while producer is creating an exit call, we need to make sure the exit call happens during a BT");
                    return null;
                }
                Map<String,String> map = new HashMap();
                exitCall = transaction.startExitCall( "exitCall"+className+methodName,
                        "iSDK  exit call" ,    ExitTypes.CUSTOM, false);
                correlationHeader = exitCall.getCorrelationHeader();
//                exitCall = transaction.startExitCall(map,methodName, ExitTypes.CUSTOM, false);
//                correlationHeader=exitCall.getCorrelationHeader();
                System.out.println( "appdynamics================== correlationHeader="+correlationHeader);
                paramValues[0] = correlationHeader; //this is rewriting the parameter to be the exit call correlation header before the method executes
                System.out.println("appdynamics================== paramValues[0]="+paramValues[0]);
                break;
            }
            case "getPackagesCor": {// consumer, create transaction
                transaction = AppdynamicsAgent.getTransaction();
                System.out.println("appdynamics================== correlationHeader="+correlationHeader);
                if( "".equals(transaction.getUniqueIdentifier()) ) { //transaction is not real, start one
                    String btName = String.format("GetPackages.%s.%s", typeId, contractTypeId); //please set this to something more meaningful for you
                    transaction = AppdynamicsAgent.startTransaction(btName, correlationHeader, EntryTypes.POJO, false);
                    getLogger().info(String.format("Transaction not active, starting a new transaction named: %s with UniqueIdentifier: %s",btName, transaction.getUniqueIdentifier())); //change to debug once you are happy with this working
                } else {
                    getLogger().info(String.format("Transaction is active, UniqueIdentifier: %s", transaction.getUniqueIdentifier())); //change to debug once you are happy with this working
                }
                break;
            }
        }
        if( transaction == null ) transaction = AppdynamicsAgent.getTransaction(); //this may end up being a fake transaction, but we will need it later, so let's get the fake one worst case scenario
        return new State(transaction, exitCall);
    }
    @Override
    public void onMethodEnd(Object state, Object invokedObject, String className, String methodName, Object[] paramValues, Throwable thrownException, Object returnValue) {
        String correlationHeader = null;
        if( paramValues.length > 0 ) correlationHeader = (String) paramValues[0];
        getLogger().info(String.format("onMethodEnd called for %s.%s( %s, ...)", className, methodName, correlationHeader)); //change to debug once you are happy with this working
        if( state == null ) return; //no op because we didn't return a state after the onMethodBegin, most likley line 66
        Transaction transaction = ((State)state).transaction;
        ExitCall exitCall = ((State)state).exitCall;
        if( thrownException != null ) {
            transaction.markAsError(thrownException.toString());
        }
        switch (methodName) {
            case "getPackagesBaseCor": {// producer, created exit call
                exitCall.end();
                break;
            }
            case "getPackagesCor": {// consumer, created transaction
                try {
                    String errorMessage = (String) getErrorMessage.execute( invokedObject.getClass().getClassLoader(), invokedObject );
                    if( errorMessage != null && !"".equals(errorMessage) ) {
                        transaction.markAsError(String.format("ResultObject is in an error state, with message: '%s'",errorMessage));
                    }
                } catch (ReflectorException e) {
                    getLogger().info("We need to edit this plugin code and put in place the correct method for getting the error message from a ResultObject on line 23 :)");
                }
                transaction.end();
                break;
            }
        }
    }

    public class State {
        public Transaction transaction;
        public ExitCall exitCall;
        public State( Transaction t, ExitCall e) {
            transaction=t;
            exitCall=e;
        }
    }

}
