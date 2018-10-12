package remoteTicketAdapterProxy

import cmdb.Customer
import cmdb.RoleType
import cmdb.Site
import grails.async.Promise
import grails.async.Promises
import grails.core.GrailsApplication
import grails.gorm.services.Service
import grails.util.Holders
import groovy.json.JsonSlurper
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import portfolioModel.ProductOffering
import ticketModel.BillOfMaterials
import ticketModel.LineItem
import ticketModel.ServiceRequest

import javax.annotation.PostConstruct

//import remoteModel.cmdbModel.Customer
//import remoteModel.ticketModel.ServiceRequest

import java.time.LocalDateTime

//@Service
//@Transactional
class RemoteRequestService {

    //cheat inection not working - inject by hand
    GrailsApplication grailsApplication  = Holders.grailsApplication
    RequestTicketClientAdapterVerticle remoteRequestClient
    def uri

    boolean initialised = false


    @PostConstruct
    def initialise () {
        uri = grailsApplication.config.getProperty( "ticketAdapterUI.request.defaultUri")

        assert remoteRequestClient //= new RequestTicketClientAdapterVerticle() //grailsApplication.config.remoteRequestClient
        remoteRequestClient.configureHttpClient()
        initialised = true
    }

    Promise getJsonRequestList() {
        if (!initialised)
            initialise ()
        Promise<JsonObject> result = Promises.createPromise()

        remoteRequestClient.apiGet(uri) { HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
            //result << json  //set DF result
        }

        result
    }

    //get the size of the remote request list
    Promise<JsonObject> getRequestListSize() {
        if (!initialised) {
            initialise()
            println "initialised http client when calling getRequestListSize()"
        }
        Promise result = Promises.createPromise()

        println "issued remote getRequestList size on $remoteRequestClient.host:$remoteRequestClient.port/$uri/count"
        remoteRequestClient.apiGet("$uri/count") { HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
        }

        println "return promise for size from getRequestListSize()"

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
        println "service bind to request list received : $graph"

        def result
        result = rlist.collect {row ->

            def name = row.entityData.attributes.customer.name
            def rcid = row.entityData.attributes.customer.id
            //Customer cust = new Customer(id:id, name:name)
            //check if customer doesnt already exists in the Domain cache
            def domainCustomer
            if (!(domainCustomer = Customer.findByRid(rcid))) {
                println "customer is not in cache to create it using rid :$rcid"
                //new customer detected to save it
                customer = new Customer()
                customer.rid = rcid
                customer.name = name
                customer.role = RoleType.CUSTOMER
                domainCustomer = customer.save(failOnError:true)
                println "cached customer  : $domainCustomer"

            }

            def rid = Long.parseLong(row.entityData.id)
            //if service request is not in the cache
            def sr
            if (!(sr = ServiceRequest.findByRid (rid))) {
                //ServiceRequest request = new ServiceRequest()
                println "SR is not in cache using rid lookup :$rid"

                serviceRequest = new ServiceRequest ()
                serviceRequest.rid = Long.parseLong(row.entityData.id)  //use top level entry under data
                serviceRequest.dateCreated = LocalDateTime.parse(row.entityData.attributes?.createdDate.value)
                serviceRequest.title = row.entityData.attributes?.title.value
                serviceRequest.customerSummary = row.entityData.attributes?.customer.value
                serviceRequest.status = row.entityData.attributes?.status.value
                serviceRequest.requestIdentifier = row.entityData.attributes?.requestIdentifier.value
                serviceRequest.priority = row.entityData.attributes?.priority.value
                if (domainCustomer)
                    domainCustomer.addToRequests(serviceRequest)

                def rBom = row.entityData.attributes?.bom
                if (rBom) {
                    if (rBom.value instanceof String) {
                        //summary form of Bom
                        BillOfMaterials bom = new BillOfMaterials ()
                        bom.value = rBom.value
                        bom.basket = new HashMap<Site, List<LineItem>>()
                        serviceRequest.setBom (bom)
                        println "SR has a bom but is not in cache, added default Bom "

                    }

                        //serviceRequest.bom = bindMapToDomainBom(rBom)
                }
                sr = serviceRequest.save (failOnError: true )
                println "SR is not in cache to create it using rid :$rid"


            } else {
                println "SR was in cache, now updaing  using rid lookup :$rid"
                //if you read serviceRequest first need to back back fill on summary
                sr.customerSummary = row.entityData.attributes?.customer.value
                sr.status = row.entityData.attributes?.status.value
                sr.title = row.entityData.attributes?.title.value
                sr.save(failOnError: true )
                println "updated cached customer  : $domainCustomer"
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



    ServiceRequest bindJsonToDomainServiceRequestEntity(JsonObject json) {
        JsonSlurper slurper = new JsonSlurper ()

        Map graph = slurper.parseText (json.encode ())
        def entity = graph.entityData
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

        //check if bom present
        def rBomDetail = entity.attributes?.bom.value.data
        if (rBomDetail) {
            def rBasket = rBomDetail?.mapAttributes?.basket
            BillOfMaterials bom
            if (serviceRequest.bom == null)
                bom = new BillOfMaterials()
            else
                bom = serviceRequest.bom
            if (bom.basket == null)
                bom.basket = new HashMap<Site, ArrayList<LineItem>>()


            //todo iterate through each site in remote bom and build listItems per site
            for (entry in rBasket.withMapEntries) {
                assert entryKey?.entityData?.entityType == "com.softwood.cmdb.Site"
                def bomBasketSiteKeyId = entry.key.entityData.id
                Site site = Site.findByRid (id)
                if (!site) {
                    //create one and save it
                }
                List<LineItem> bomSiteLineItems
                if (!bom.basket.containsKey(site)) {
                    bom.addSite(site)
                }
                bomSiteLineItems = bom.basket[site]
                bom.basket.save (failOnError:true)

                List<LineItem> lineItems =  bom.basket[site]

                def rLines = entry.value
                //for each line from remote bom
                for (rline in rLines) {

                    def lineNumber = rLine?.attributes?.lineNumber.value
                    def matchedLine = lineItems.find {it.rLineId == lineNumber}
                    def quantity = rLine?.attributes?.quantity.value
                    def status = rLine?.attributes?.status.value
                    def offering = rLine?.attributes?.offering
                    if (!matchedLine) {
                        matchedLine = new LineItem()
                        matchedLine.rLineId = lineNumber
                    }
                    //update or save entry
                    matchedLine.quantity = quantity
                    matchedLine.status = status
                    def productOffering
                    productOffering = ProductOffering.findByPid (rline.attributes.offering.id)
                    //if not cached then add to product cache
                    if (!productOffering) {
                        productOffering = new ProductOffering()
                        productOffering.pid = rline.attributes.offering.id
                        productOffering.name = rline.attributes.offering.name
                        productOffering.save(failOnError:true)
                    }
                    matchedLine.offering = productOffering
                    matchedLine.save (failOnError:true)

                }
            }

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


        remoteRequestClient.apiGet("$uri/$id") { HttpResponse response  ->
            result.accept(response.bodyAsJsonObject())
        }

        result
    }
}
