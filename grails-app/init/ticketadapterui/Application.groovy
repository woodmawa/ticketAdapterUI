package ticketadapterui

import grails.async.Promise
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.core.GrailsApplication
import grails.util.Holders
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import remoteTicketAdapterProxy.RemoteRequestService

import java.util.concurrent.TimeUnit

class Application extends GrailsAutoConfiguration {
    //inject my special remote service handler bean into controller
    static RemoteRequestService remoteRequestService
    static WebClient client
    static Vertx vertx
    static String error
    static def host
    static def port


    static void main(String[] args) {
        GrailsApp.run(Application, args)

        GrailsApplication app = Holders.grailsApplication
        remoteRequestService = app.getMainContext().getBean("remoteRequestService")
        assert remoteRequestService
        println "got remoteRequestService $remoteRequestService from bean context"
        Promise p //=  remoteRequestService.getRequestListSize()
        buildVertxClient()
        println "try local local get instead on localhost:8082/api/request/count"
        apiGet("localhost:8082/api/request/count") {handlerResult ->
            p.accept(handlerResult)
        }
        def json
        Long rRequestListSize
        try {
             json = p.get(2, TimeUnit.SECONDS)
             rRequestListSize = json.getLong("requestListSize")
            println "first local attempt received : $json"
        } catch (java.util.concurrent.TimeoutException ex) {
            println "swallowed exception : " + ex.printStackTrace()
        }

        try {
            p = remoteRequestService.getRequestListSize()
            json = p.get(2, TimeUnit.SECONDS)
            rRequestListSize = json.getLong("requestListSize")
            println "second attempt received : $json"
        } catch (java.util.concurrent.TimeoutException ex) {
            println "swallowed second  exception : " + ex.printStackTrace()
        }

    }

    static def buildVertxClient () {


        WebClientOptions options = new WebClientOptions() //[userAgent:"", ]
        if (!vertx)
            vertx = Vertx.vertx()//grailsApplication.vertx

        if (!host) {
            host = Holders.grailsApplication.config.ticketAdapterUI.request.host
            port = Holders.grailsApplication.config.ticketAdapterUI.request.port
        }

        options.setDefaultHost(host)
        options.setDefaultPort(port)

        client = WebClient.create(vertx, options)
        client
    }

    static HttpRequest apiGet (String uri, Closure handler ) {
        client.get(port, host, uri).send {ar ->
            HttpResponse<Buffer> getResult
            if (ar.succeeded()) {
                //obtain the response
                getResult = ar.result()
            } else {
                error = ar.cause().getMessage()
            }
            handler (getResult)
        }
    }


}