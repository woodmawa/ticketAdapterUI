package ticketModel

import portfolioModel.ProductOffering

class LineItem {
    long rLineId
    ProductOffering offering
    //ConfigurationItem productInstance       //optional if provide
    int quantity
    String status
    Date shippingDate

    static constraints = {
        rLineId nullable :true
        offering nullable :true
        quantity ()
        status nullable:false
        shippingDate nullable:true
    }

    //static belongsTo = BillOfMaterials  //adds addTo methods on the owner side
}
