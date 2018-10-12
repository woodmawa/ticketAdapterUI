package portfolioModel

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class ProductOfferingServiceSpec extends Specification {

    ProductOfferingService productOfferingService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new ProductOffering(...).save(flush: true, failOnError: true)
        //new ProductOffering(...).save(flush: true, failOnError: true)
        //ProductOffering productOffering = new ProductOffering(...).save(flush: true, failOnError: true)
        //new ProductOffering(...).save(flush: true, failOnError: true)
        //new ProductOffering(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //productOffering.id
    }

    void "test get"() {
        setupData()

        expect:
        productOfferingService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<ProductOffering> productOfferingList = productOfferingService.list(max: 2, offset: 2)

        then:
        productOfferingList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        productOfferingService.count() == 5
    }

    void "test delete"() {
        Long productOfferingId = setupData()

        expect:
        productOfferingService.count() == 5

        when:
        productOfferingService.delete(productOfferingId)
        sessionFactory.currentSession.flush()

        then:
        productOfferingService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        ProductOffering productOffering = new ProductOffering()
        productOfferingService.save(productOffering)

        then:
        productOffering.id != null
    }
}
