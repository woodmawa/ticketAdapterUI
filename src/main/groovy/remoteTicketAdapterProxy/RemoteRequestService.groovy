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
import io.vertx.ext.web.client.HttpResponse
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
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
    RequestTicketClientAdapter remoteRequestClient
    String  uri

    boolean initialised = false


    @PostConstruct
    def initialise () {
        uri = grailsApplication.config.getProperty( "ticketAdapterUI.request.defaultUri")
        println "post contruct invoked on RemoteRequestService set uri as $uri"

        assert remoteRequestClient
     }

    JSONElement getJsonRequestList(String uri) {

        remoteRequestClient.apiGet("$uri")

    }


    //get the size of the remote request list
    long getRequestListSize(uri) {
        JSONObject json = remoteRequestClient.apiGet("$uri/count")
        json.getLong("requestListSize")
    }

    //hand crafted for now - returns list of cached Domain objects
    //persisted in local gorm cache repository to drive the UI
    def bindJsonToDomainServiceRequestList(JSONObject json) {

        //create unsaved domain objects
        Customer customer
        ServiceRequest serviceRequest

        JsonSlurper slurper = new JsonSlurper ()

        String resList = json.toString()
        Map graph = slurper.parseText (json.toString())
        List rlist = graph.requestList
        println "service bind to request list received : $rlist with size ${rlist.size()}"

        def result
        result = rlist.collect {row ->

            println "processing row : $row"
            def name = row.entityData?.attributes?.customer.name
            def rcid = row.entityData?.attributes?.customer.id
            assert name
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
                serviceRequest.customerSummary = domainCustomer.toString()
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
                println "SR is not in cache, tried to create :${sr.toString()}"


            } else {
                println "SR was in cache, now updaing  using rid lookup :$rid"
                //if you read serviceRequest first need to back back fill on summary
                sr.customerSummary = domainCustomer.toString()
                sr.status = row.entityData.attributes?.status.value
                sr.title = row.entityData.attributes?.title.value
                sr.save(failOnError: true )
                println "updated cached SR  : $sr"
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



    ServiceRequest bindJsonToDomainServiceRequestEntity(JSONObject json) {
        JsonSlurper slurper = new JsonSlurper ()

        Map graph = slurper.parseText (json.toString ())
        def entity = graph.entityData
        def type = entity.type
        def sr_id = entity.id

        Customer domainCustomer
        if (entity?.attributes?.customer){
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
        BillOfMaterials bom
        def rBomDetail = entity.attributes?.bom.value.data
        if (rBomDetail) {
            def rBasket = rBomDetail?.mapAttributes?.basket
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
            println "adding bom to cache : $bom  with basket size ${bom.basket.size()}"
            bom.save(failOnError:true)
        }

        serviceRequest.bom = bom
        //update cached SR entry from server
        println "updating cached SR from server"
        serviceRequest.save (failOnError: true )

    }

    JSONObject getJsonRequestById( Long id) {
        remoteRequestClient.apiGet("$uri/$id")
    }
}
