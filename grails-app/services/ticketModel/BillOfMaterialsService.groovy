package ticketModel

import grails.gorm.services.Service

@Service(BillOfMaterials)
interface BillOfMaterialsService {

    BillOfMaterials get(Serializable id)

    List<BillOfMaterials> list(Map args)

    Long count()

    void delete(Serializable id)

    BillOfMaterials save(BillOfMaterials billOfMaterials)

}