package ticketModel

import cmdb.Site
import portfolioModel.ProductOffering

import javax.persistence.Transient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class BillOfMaterials {
    //site is expected index into the map,  with array of lineItems as value type
    Map basket = new HashMap<Site, List<LineItem>>()
    String value
    transient AtomicInteger numberOfLines = new AtomicInteger(0)

    static belongsTo = [serviceRequest: ServiceRequest]

    static constraints = {
        basket nullable:false
        value nullable:true
    }

    //static belongsTo = ServiceRequest  //add methods but one driectional from request to bom

    void clear () {
        basket.clear()
    }

    def addSite (Site site) {
        assert site
        if (basket.containsKey(site))
            throw new UnsupportedOperationException ("site key '$site', already exists in this basket")
        basket.put (site, new LinkedList<LineItem>() )
    }

    void addToBasket (Site site, ProductOffering po, quantity=1) {
        LineItem line = new LineItem (offering:po)
        line.status = "preOrder"
        line.quantity = quantity
        Queue lines = basket[site]
        numberOfLines++  //does this work ?
        lines << line
    }

    /*void addToBasket (Site, ConfigurationItem ci) {
        LineItem line = new LineItem (offering:ci.offering, productInstance: ci)
        line.status = "preOrder"
        line.lineNumber = ++lineNumberGenerator
        line.quantity = 1
        Queue lines = basket[site]
        lines << line
    }*/

    //return Collection of lines in this basket for site
    Collection getSiteItemsList (Site site) {
        if (basket.containsKey(site)) {
            basket.get(site)

        }
    }

    Collection getAllItemsList () {
        def allLines = []
        basket.each { key, value ->
            allLines << value.toList()
        }

        allLines
    }

    /**
     * each entry starts with site, then itemLines
     * @return
     */
    Collection getAllItemsBySiteSortedList () {
        def allLines = []
        basket.each {key, value ->

            allLines << site
            allLines << value.toList()
            allLines.sort  {a,b -> a[0] <=> b[0]}
        }
        allLines.collect {it[1]}
    }

    String toString ()  {
        "BillOfMaterials (number of LineItems : $numberOfLines)"
    }
}
