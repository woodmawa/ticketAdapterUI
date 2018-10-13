package remoteTicketAdapterProxy

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.util.Holders
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import ticketModel.ServiceRequest

//@Service
//@Transactional
class RequestTicketClientAdapterVerticle extends AbstractVerticle implements Verticle, GrailsConfigurationAware {

    String name
    WebClient client
    String error = ""
    Vertx vertx
    String host
    int port
    Config config
    long timeout = 3000 //default 3 seconds timeout for gets

    def grailsApplication  = Holders.grailsApplication  //covers for no injection on non grails artifact class types

    //grails should inject the config
    void setConfiguration (Config conf) {
        config = conf
    }

    void start(Future<Void> future) {
        println "starting Request Api Client .. "
        //client = configureHttpClient()

        println "started Request Api Client "
        future.complete()
    }

    void stop(Future<Void> future) {
        println "stopped Request client "
        future.complete()

    }

    void configureHttpClient() {
        WebClientOptions options = new WebClientOptions() //[userAgent:"", ]
        if (!vertx)
            vertx = Vertx.vertx()//grailsApplication.vertx

        if (!host) {
            host = grailsApplication.config.ticketAdapterUI.request.host
            port = grailsApplication.config.ticketAdapterUI.request.port
        }

        options.setDefaultHost(host)
        options.setDefaultPort(port)

        client = WebClient.create(vertx, options)
        client

    }



    /**
     * post messaging
     * @param uri
     * @param host
     * @param port
     * @return
     */

    //todo will need fix post again - needs RemoteServiceRequest class i think

    HttpRequest apiPost (String uri, ServiceRequest requestTicket, Closure handler = {}) {


        JsonObject postBody =  $convertTicketToSnowPostFormat (requestTicket)
        apiPost (client.post(uri), postBody, handler)
    }

    HttpRequest apiPost (String uri, JsonObject postBody, Closure handler = {}) {
        apiPost (client.post(uri), postBody, handler)
    }

    HttpRequest apiPost (String uri, String bodyString, Closure handler = {}) {

        JsonObject jsonBody = new JsonObject(bodyString)
        apiPost (client.post(uri), jsonBody,  handler)
    }

    HttpRequest apiPost (String uri, def object, Closure handler = {}) {

        JsonObject jsonBody = new JsonObject (Json.encode(object))
        apiPost (client.post(uri), jsonBody, handler)
    }

    HttpRequest apiPost (HttpRequest<Buffer> request, JsonObject jsonBody, Closure handler = {}) {
        request.host(host).port(port)
        request.putHeader("accept", "application/text")
        request.putHeader("content-type", "application/json" )
        request.putHeader("content-length", "${jsonBody.encode().size()}" )
        request.method (HttpMethod.POST)
        request

        request.sendJsonObject(jsonBody) {ar ->
            HttpResponse<Buffer> postResult
            if (ar.succeeded()) {
                //obtain the response
                postResult = ar.result()
            } else {
                error = ar.cause().getMessage()
            }
            handler (postResult)
        }
        request

    }

    /**
     * get messaging
     * @param uri
     * @param queryParams
     * @return
     */

    HttpRequest<Buffer> apiAddQueryparams (String uri, queryParams = [:] ) {
        queryParams.each {
            client.get(uri).addQueryParam(it.key, it.value)

        }
        HttpRequest<Buffer> request = client.get()
    }



    HttpRequest apiGet (String uri, Closure handler ) {
        error = "" //reset errors
        HttpRequest<Buffer> request = client.get(port, host, uri)
        request.putHeader("accept", "application/text")
        request.timeout(timeout)
        println "get request dump: " +  request.dump()
        request.send {ar ->
            println "ar -> is of type :" + ar.getClass()
            println "sent request [Get] to host: $host:${port}/$uri to server"
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

    //use grails rest client rather than vertx one
    def remoteApiGet (String uri) {
        RestBuilder rest = new RestBuilder ()
        def url = "http://$host:$port/$uri"

        RestResponse restResponse = rest.get (url)
        println "sent get request to  : $url "
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            return restResponse.json
        }
        else
            null
    }

    void apiGet (HttpRequest<Buffer> request, Closure handler) {
        println "sending request [$request.method] to host: ${request.host}:${request.port}" + request.uri + " to server"
        request.host(host).port(port)
        request.timeout(timeout)
        request.send {ar ->
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

/*   void apiSendPost(HttpRequest<Buffer> request, JsonObject postBody, Closure handler) {
        request.sendJsonObject(postBody) {ar ->
            HttpResponse<Buffer> postResult
            if (ar.succeeded()) {
                //obtain the response
                postResult = ar.result()
            } else {
                error = ar.cause().getMessage()
            }
            handler (postResult)
        }
    }

        void apiSend (HttpRequest<Buffer> request, JsonObject reqBody, Closure handler) {
        println "sending request [$request.method] to host: ${request.host}:${request.port}" + request.uri + " to server"
        switch (request.method) {
            case  HttpMethod.GET :
                request.send {ar ->
                    HttpResponse<Buffer> getResult
                    if (ar.succeeded()) {
                        //obtain the response
                        getResult = ar.result()
                    } else {
                        error = ar.cause().getMessage()
                    }
                    handler (getResult)
                }
                break
            case HttpMethod.POST :
                request.sendJsonObject(reqBody) {ar ->
                    HttpResponse<Buffer> postResult
                    if (ar.succeeded()) {
                        //obtain the response
                        postResult = ar.result()
                    } else {
                        error = ar.cause().getMessage()
                    }
                    handler (postResult)
                }
        }

    } */



    boolean hasError () {
        error ? true : false
    }

    void clearError () {
        error = ""
    }

}
