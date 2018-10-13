package ticketadapterui

import grails.async.Promise
import grails.async.Promises
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.core.GrailsApplication
import grails.util.Holders
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import remoteTicketAdapterProxy.RemoteRequestService

import java.util.concurrent.TimeUnit

class Application extends GrailsAutoConfiguration {
    //inject my special remote service handler bean into controller
    static RemoteRequestService remoteRequestService
    static String error
    static def host
    static def port


    static void main(String[] args) {
        GrailsApp.run(Application, args)

        /*GrailsApplication app = Holders.grailsApplication
        remoteRequestService = app.getMainContext().getBean("remoteRequestService")
        assert remoteRequestService
        println "got remoteRequestService $remoteRequestService from bean context"
        def json
        Long rRequestListSize

        rRequestListSize = remoteRequestService.getRequestListSize("api/request/count")

        println "size of requests is : $rRequestListSize"

        JSONElement reqList = remoteRequestService.getJsonRequestList()
        println "got request list : " + reqList.toString()  */

    }



}