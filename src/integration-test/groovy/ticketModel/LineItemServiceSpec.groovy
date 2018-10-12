package ticketModel

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import org.hibernate.SessionFactory

@Integration
@Rollback
class LineItemServiceSpec extends Specification {

    LineItemService lineItemService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new LineItem(...).save(flush: true, failOnError: true)
        //new LineItem(...).save(flush: true, failOnError: true)
        //LineItem lineItem = new LineItem(...).save(flush: true, failOnError: true)
        //new LineItem(...).save(flush: true, failOnError: true)
        //new LineItem(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //lineItem.id
    }

    void "test get"() {
        setupData()

        expect:
        lineItemService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<LineItem> lineItemList = lineItemService.list(max: 2, offset: 2)

        then:
        lineItemList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        lineItemService.count() == 5
    }

    void "test delete"() {
        Long lineItemId = setupData()

        expect:
        lineItemService.count() == 5

        when:
        lineItemService.delete(lineItemId)
        sessionFactory.currentSession.flush()

        then:
        lineItemService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        LineItem lineItem = new LineItem()
        lineItemService.save(lineItem)

        then:
        lineItem.id != null
    }
}
