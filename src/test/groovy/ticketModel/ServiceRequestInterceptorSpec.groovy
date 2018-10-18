package ticketModel

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ServiceRequestInterceptorSpec extends Specification implements InterceptorUnitTest<ServiceRequestInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test serviceRequest interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"serviceRequest")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
