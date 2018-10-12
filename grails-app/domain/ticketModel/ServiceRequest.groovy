package ticketModel

import cmdb.Customer
import java.time.LocalDateTime

class ServiceRequest {

    String rid
    String requestIdentifier
    String title
    String customerSummary
    String status
    LocalDateTime dateCreated = LocalDateTime.now()
    LocalDateTime requiredDate
    Date authorisedDate
    String contactDetails
    String priority = "normal"
    BillOfMaterials bom

    static belongsTo = [customer:Customer]

    //static hasOne = [bom: BillOfMaterials] //- see https://stackoverflow.com/questions/28089830/grails-hasone-but-does-not-belong-to

    String toString() {
        "ServiceRequest (rid:$rid, customer:$customerSummary, status:$status)"
    }

    static constraints = {
        rid  nullable:false, readOnly:true
        requestIdentifier size:3..30, unique:true, nullable:false
        title nullable:false
        customer nullable:true
        customerSummary nullable:true
        status nullable :false
        dateCreated nullable:false
        requiredDate nullable:true
        authorisedDate nullable:true
        contactDetails nullable:true
        priority nullable:true
        bom nullable:true

    }

}
