package ticketModel

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class BillOfMaterialsController {

    static scaffold = BillOfMaterials

    BillOfMaterialsService billOfMaterialsService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond billOfMaterialsService.list(params), model:[billOfMaterialsCount: billOfMaterialsService.count()]
    }

    def show(Long id) {
        respond billOfMaterialsService.get(id)
    }

    def create() {
        respond new BillOfMaterials(params)
    }

    def save(BillOfMaterials billOfMaterials) {
        if (billOfMaterials == null) {
            notFound()
            return
        }

        try {
            billOfMaterialsService.save(billOfMaterials)
        } catch (ValidationException e) {
            respond billOfMaterials.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'billOfMaterials.label', default: 'BillOfMaterials'), billOfMaterials.id])
                redirect billOfMaterials
            }
            '*' { respond billOfMaterials, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond billOfMaterialsService.get(id)
    }

    def update(BillOfMaterials billOfMaterials) {
        if (billOfMaterials == null) {
            notFound()
            return
        }

        try {
            billOfMaterialsService.save(billOfMaterials)
        } catch (ValidationException e) {
            respond billOfMaterials.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'billOfMaterials.label', default: 'BillOfMaterials'), billOfMaterials.id])
                redirect billOfMaterials
            }
            '*'{ respond billOfMaterials, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        billOfMaterialsService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'billOfMaterials.label', default: 'BillOfMaterials'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'billOfMaterials.label', default: 'BillOfMaterials'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
