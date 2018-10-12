package portfolioModel

class ProductOffering {

    String pid
    String name
    String type
    String hierarchy

    static constraints = {
        pid nullable:false
        name nullable:false
        type nullable:false
        hierarchy nullable:true
    }

    String toString() {
        "Productoffering (name:$name, type:type) [pid:$pid]"
    }
}
