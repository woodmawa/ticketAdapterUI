package portfolioModel

import grails.gorm.services.Service

@Service(ProductOffering)
interface ProductOfferingService {

    ProductOffering get(Serializable id)

    List<ProductOffering> list(Map args)

    Long count()

    void delete(Serializable id)

    ProductOffering save(ProductOffering productOffering)

}