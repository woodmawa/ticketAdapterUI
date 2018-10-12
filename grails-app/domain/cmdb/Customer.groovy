package cmdb

import ticketModel.ServiceRequest

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

class Customer {

    String rid
    String name
    RoleType role
    Date dateCreated = Date.newInstance()//= LocalDateTime.now()
    //List sites //= new ConcurrentLinkedQueue()
//    ConcurrentLinkedQueue<Contract> contracts = new ConcurrentLinkedQueue()
    //List<ServiceRequest> requests //= new ConcurrentLinkedQueue()

    static constraints = {
        rid nullable :false
        name nullable : false
        role nullable :false
        dateCreated nullable:false
        sites nullable:true
        requests nullable:true
    }

    //static hasMany = [requests:ServiceRequest]
    static hasMany = [sites:Site, requests:ServiceRequest]
    //static hasMany = [sites:Site]

    String toString() {
        "Customer ( name:$name) [rid:$rid]"
    }
}
