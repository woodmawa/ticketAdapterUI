package remoteTicketAdapterProxy

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
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
import org.grails.web.json.JSONElement
import ticketModel.ServiceRequest

import javax.annotation.PostConstruct


class RequestTicketClientAdapter extends AbstractVerticle implements Verticle, GrailsConfigurationAware {

    String name
    String error = ""
    String host
    int port
    Config config  //grails config
    RestBuilder client
    String protocol
    String baseUrl

    def grailsApplication  = Holders.grailsApplication  //covers for no injection on non grails artifact class types

    //grails should inject the config
    void setConfiguration (Config conf) {
        config = conf
    }


    @PostConstruct
    void configureHttpClient() {
        baseUrl = "$protocol://$host:$port"
        println "post construct RequestTicketClientAdapter built baseUrl : $baseUrl"
    }


    //now using com.google.gson.Element parent class for
    JSONElement apiGet (String uri) {
        error = ""  //reset errors
        RestBuilder rest = new RestBuilder ()
        def url = "$baseUrl/$uri"

        println "apiGet calling get on $url"
        RestResponse restResponse = rest.get (url)
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            println "return " + restResponse.json
            return restResponse.json
        }
        else {
            println "error " + restResponse.getStatusCode()
            error = "${restResponse.getStatusCode()}"
            null
        }
    }



    boolean hasError () {
        error ? true : false
    }

    void clearError () {
        error = ""
    }

}
