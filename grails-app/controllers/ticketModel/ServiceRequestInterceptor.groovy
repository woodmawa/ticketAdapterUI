package ticketModel

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import org.grails.web.json.JSONObject
import remoteTicketAdapterProxy.RemoteRequestService

//permit interceptor to access grails config
class ServiceRequestInterceptor implements GrailsConfigurationAware {

    RemoteRequestService remoteRequestService
    ServiceRequestService serviceRequestService

    ServiceRequestInterceptor () {
        //match controller:'ServiceRequest'
    }

    //injected grails app config
    @Override
    void setConfiguration(Config co) {
        // configure the interceptor matching dynamically
        // based on what is in application.yml
        //match co.'demo.interceptor.first'
    }

    boolean before() {
        String uri = this.getRequest().requestURI
        String[] parts = uri.split("/")
        println "in before interceptor with uri : '$uri', with uri parts : $parts "
        long rReqCount
        if (parts.size() == 1 || (parts.size() > 1 && parts[-1] == "index") ) {
            String reqApiCount = grailsApplication.config.ticketAdapterUI.request.defaultUri + "/count"
            rReqCount  = remoteRequestService.getRequestListSize(reqApiCount)
            println "found $rReqCount remote requests"
            if (rReqCount > serviceRequestService.count()) {
                JSONObject result = remoteRequestService.getJsonRequestList(grailsApplication.config.ticketAdapterUI.request.defaultUri)
                remoteRequestService.bindJsonToDomainServiceRequestList(result)
                println "refreshed SR cache from remote server "
            }
        }
        if (parts.size() > 2 && parts[-2] == "show") {
            def sr_id = Long.parseLong(parts[-1])
            ServiceRequest sr = ServiceRequest.get (sr_id)
            JSONObject json
            if (sr) {
                json = remoteRequestService.getJsonRequestById(Long.parseLong(sr.rid))
                remoteRequestService.bindJsonToDomainServiceRequestEntity(json)
                println "refreshed SR cache from remote server "

            }

        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
