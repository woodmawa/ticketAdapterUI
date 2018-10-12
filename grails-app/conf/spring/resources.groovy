// Place your Spring DSL code here


import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import remoteTicketAdapterProxy.RemoteRequestService
import remoteTicketAdapterProxy.RequestTicketClientAdapterVerticle
import ticketModel.ServiceRequest
import java.time.LocalDateTime

beans = {

    //declare the remote classes as Spring beans
    //see if this allows fields plugin to render it
    remoteRequestService(RemoteRequestService) {
        uri = grailsApplication.config.getProperty( "ticketAdapterUI.request.defaultUri")
        remoteRequestClient = ref ("remoteRequestClient")   //inject remote request client
    }

    //setup remote vertx request bean
    remoteRequestClient (RequestTicketClientAdapterVerticle) {
        host = grailsApplication.config.ticketAdapterUI.request.host
        port = grailsApplication.config.ticketAdapterUI.request.port
        vertx = Vertx.vertx()
        WebClientOptions  options = new WebClientOptions()
        options.setDefaultHost(host)
        options.setDefaultPort(port)
        client = WebClient.create(vertx, options)

    }
}
