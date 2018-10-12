package ticketModel

import grails.gorm.services.Service

@Service(LineItem)
interface LineItemService {

    LineItem get(Serializable id)

    List<LineItem> list(Map args)

    Long count()

    void delete(Serializable id)

    LineItem save(LineItem lineItem)

}