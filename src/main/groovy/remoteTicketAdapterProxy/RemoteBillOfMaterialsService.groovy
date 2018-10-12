package remoteTicketAdapterProxy

import cmdb.Customer
import cmdb.RoleType
import cmdb.Site
import grails.async.Promise
import grails.async.Promises
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import ticketModel.BillOfMaterials
import ticketModel.LineItem
import ticketModel.ServiceRequest

import javax.annotation.PostConstruct
import java.time.LocalDateTime

//import remoteModel.cmdbModel.Customer
//import remoteModel.ticketModel.ServiceRequest

//@Transactional
class RemoteBillOfMaterialsService {

    //inject beans
    GrailsApplication grailsApplication
    //inject private data transfer objects
    //remoteModel.cmdbModel.Customer remoteCustomer
    //remoteModel.ticketModel.ServiceRequest  remoteServiceRequest

    RequestTicketClientAdapterVerticle requestClient
    def uri

    boolean initialised = false


    @PostConstruct
    def initialise () {
        uri = grailsApplication.config.getProperty( "ticketAdapterUI.request.defaultUri")

        requestClient = new RequestTicketClientAdapterVerticle() //grailsApplication.config.remoteRequestClient
        requestClient.configureHttpClient()
        initialised = true
    }

    Promise getJsonRequestList() {
        if (!initialised)
            initialise ()
        Promise<JsonObject> result = Promises.createPromise()

        requestClient.apiGet(uri) { HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
            //result << json  //set DF result
        }

        result
    }

    //get the size of the remote request list
    Promise<JsonObject> getRequestListSize() {
        if (!initialised)
            initialise ()
        Promise result = Promises.createPromise()

        requestClient.apiGet("$uri/count") { HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
        }

        result
    }

    //hand crafted for now - returns list of cached Domain objects
    //persisted in local gorm cache repository to drive the UI
    def bindJsonToDomainServiceRequestList(JsonObject json) {

        //create unsaved domain objects
        Customer customer
        ServiceRequest serviceRequest

        JsonSlurper slurper = new JsonSlurper ()

        Map graph = slurper.parseText (json.encode ())
        List rlist = graph.requestList
        def reqListSize =  rlist.size()
        assert rlist[0].data.entityType == "com.softwood.request.Request"

        def result
        result = rlist.collect {row ->

            def name = row.data.attributes.customer.name
            def id = row.data.attributes.customer.id
            //Customer cust = new Customer(id:id, name:name)
            //check if customer doesnt already exists in the Domain cache
            def domainCustomer
            if (!(domainCustomer = Customer.findByRid(id))) {
                //new customer detected to save it
                customer = new Customer()
                customer.rid = id
                customer.name = name
                customer.role = RoleType.CUSTOMER
                domainCustomer = customer.save(failOnError:true)
            }

            def rid = Long.parseLong(row.data.id)
            //if service request is not in the cache
            def sr
            if (!(sr = ServiceRequest.findByRid (rid))) {
                //ServiceRequest request = new ServiceRequest()
                serviceRequest = new ServiceRequest ()
                serviceRequest.rid = Long.parseLong(row.data.id)  //use top level entry under data
                serviceRequest.dateCreated = LocalDateTime.parse(row.data.attributes?.createdDate.value)
                serviceRequest.title = row.data.attributes?.title.value
                serviceRequest.customerSummary = row.data.attributes?.customer.value
                serviceRequest.status = row.data.attributes?.status.value
                serviceRequest.requestIdentifier = row.data.attributes?.requestIdentifier.value
                serviceRequest.priority = row.data.attributes?.priority.value
                if (domainCustomer)
                    domainCustomer.addToRequests(serviceRequest)
                sr = serviceRequest.save (failOnError: true )

                def rBom = row.data.attributes?.bom
                if (rBom)
                    serviceRequest.bom = bindMapToDomainBom (rBom)

                //request.bom = new BillOfMaterials(rlist.data.attributes.bom.value) - dont need in listing
            } else {
                //if you read serviceRequest first need to back back fill on summary
                sr.customerSummary = row.data.attributes?.customer.value
                sr.status = row.data.attributes?.status.value
                sr.title = row.data.attributes?.title.value
                sr.save(failOnError: true )
            }
            //return the serviceRequest into collection
            sr
        }
        //return list of domain object cache SR tickets
        result
    }

    private BillOfMaterials bindMapToDomainBom (Map rBom) {

        BillOfMaterials bom = new BillOfMaterials ()
        if (rBom.value instanceof String) {
            //summary form of Bom
            bom.value = rBom.value
       } else {
            //expanded record format
            Map rBasket = rBom.mapAttributes.basket

            bom.basket.value = rBom.basket.value
            bom.basket = new LinkedHashMap()
            //need to loop through basket by site keys and get the lineItem list and build the equiv
            rBasket.each { k, v ->
                //um need to check what this looks like in the json
                println "site key:$key, value :$value "
            }
        }

        bom

    }


    //hand crafted for now - this version binds to
    //local class defs in the src/main/groovy - now created
    //beans to get them injected
    def bindJsonToServiceRequestList(JsonObject json) {
        JsonSlurper slurper = new JsonSlurper ()

        Map graph = slurper.parseText (json.encode ())
        List rlist = graph.requestList
        def reqListSize =  rlist.size()

        def result
        result = rlist.collect {row ->

            def name = row.data.attributes.customer.name
            def id = row.data.attributes.customer.id
            //Customer cust = new Customer(id:id, name:name)
            remoteCustomer.id = id
            remoteCustomer.name = name

            //ServiceRequest request = new ServiceRequest()
            remoteServiceRequest.id = Long.parseLong(row.data.id)  //use top level entry under data
            remoteServiceRequest.createdDate = LocalDateTime.parse(row.data.attributes.createdDate.value)
            remoteServiceRequest.customerSummary = row.data.attributes?.customer.value
            remoteServiceRequest.status = row.data.attributes?.status.value
            remoteServiceRequest.requestIdentifier = row.data.attributes?.requestIdentifier.value
            remoteServiceRequest.priority = row.data.attributes?.priority.value
            remoteCustomer.addRequest(remoteServiceRequest)
            //request.bom = new BillOfMaterials(rlist.data.attributes.bom.value) - dont need in listing
            remoteServiceRequest
        }
        //return list of remote SR tickets as beans
        result
    }

    ServiceRequest bindJsonToDomainServiceRequestEntity(JsonObject json) {
        JsonSlurper slurper = new JsonSlurper ()

        Map graph = slurper.parseText (json.encode ())
        def entity = graph.data
        def type = entity.type
        def sr_id = entity.id

        Customer domainCustomer
        if (entity.attributes.customer){
            //if we havent seen the customer record better create it now
            if (!(domainCustomer = Customer.findByRid((entity.attributes.customer.id)))) {
                Customer customer = new Customer ()
                customer.role = RoleType.CUSTOMER
                customer.name = entity.attributes.customer.name
                customer.rid = entity.attributes.customer.id
                domainCustomer = customer.save(failOnError:true)
            }
        }

        def rBom = entity.attributes?.bom.value

        ServiceRequest serviceRequest
        if (!(serviceRequest = ServiceRequest.findById (sr_id))) {
            serviceRequest = new ServiceRequest ()
        }
        //update the cached details regardless
        serviceRequest.rid = Long.parseLong(sr_id)  //use top level entry under data
        serviceRequest.dateCreated = LocalDateTime.parse(entity.attributes.createdDate.value)
        serviceRequest.title = entity.attributes?.title.value
        serviceRequest.status = entity.attributes?.status.value
        serviceRequest.requestIdentifier = entity.attributes?.requestIdentifier.value
        serviceRequest.priority = entity.attributes?.priority.value
        if (domainCustomer)
            domainCustomer.addToRequests(serviceRequest)

        BillOfMaterials bom
        if (rBom) {
            bom = new BillOfMaterials()
            bom.basket = new HashMap<Site, ArrayList<LineItem>>()
            //todo iterate through each site in remote bom and build listItems per site
            bom.save(failOnError:true)
        }

        serviceRequest.bom = bom
        //update cached SR entry from server
        println "updating cached SR from server"
        serviceRequest.save (failOnError: true )

    }

    Promise getJsonRequestById( Long id) {
        if (!initialised)
            initialise ()
        Promise result = Promises.createPromise()


        requestClient.apiGet("$uri/$id") {HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
        }

        result
    }
}
