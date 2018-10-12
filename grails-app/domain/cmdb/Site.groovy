package cmdb

import java.time.LocalDateTime

class Site {
    String rid
    String name
    String siteContact
    String address  //simple for now
    String postalCode
    Date dateCreated = Date.newInstance()
    //ConcurrentLinkedQueue<ConfigurationItem> inventory = new ConcurrentLinkedQueue()

    static belongsTo = [customer:Customer]

    String toString() {
        "Site (name:$name) [rid:${id.toString()}] "
    }

    static constraints = {
        rid nullable:false
        name nullable:false
        siteContact nullbale:true
        address nullable:true
        postalCode nullable:true
        customer nullable: false
        dateCreated nullable:false
    }


}
