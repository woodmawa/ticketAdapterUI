package portfolioModel

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ProductOfferingController {

    static scaffold = ProductOffering
    ProductOfferingService productOfferingService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond productOfferingService.list(params), model:[productOfferingCount: productOfferingService.count()]
    }

    def show(Long id) {
        respond productOfferingService.get(id)
    }

    def create() {
        respond new ProductOffering(params)
    }

    def save(ProductOffering productOffering) {
        if (productOffering == null) {
            notFound()
            return
        }

        try {
            productOfferingService.save(productOffering)
        } catch (ValidationException e) {
            respond productOffering.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'productOffering.label', default: 'ProductOffering'), productOffering.id])
                redirect productOffering
            }
            '*' { respond productOffering, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond productOfferingService.get(id)
    }

    def update(ProductOffering productOffering) {
        if (productOffering == null) {
            notFound()
            return
        }

        try {
            productOfferingService.save(productOffering)
        } catch (ValidationException e) {
            respond productOffering.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'productOffering.label', default: 'ProductOffering'), productOffering.id])
                redirect productOffering
            }
            '*'{ respond productOffering, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        productOfferingService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'productOffering.label', default: 'ProductOffering'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'productOffering.label', default: 'ProductOffering'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
