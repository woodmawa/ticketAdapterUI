package ticketModel

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class LineItemController {

    static scaffold = LineItem

    LineItemService lineItemService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond lineItemService.list(params), model:[lineItemCount: lineItemService.count()]
    }

    def show(Long id) {
        respond lineItemService.get(id)
    }

    def create() {
        respond new LineItem(params)
    }

    def save(LineItem lineItem) {
        if (lineItem == null) {
            notFound()
            return
        }

        try {
            lineItemService.save(lineItem)
        } catch (ValidationException e) {
            respond lineItem.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'lineItem.label', default: 'LineItem'), lineItem.id])
                redirect lineItem
            }
            '*' { respond lineItem, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond lineItemService.get(id)
    }

    def update(LineItem lineItem) {
        if (lineItem == null) {
            notFound()
            return
        }

        try {
            lineItemService.save(lineItem)
        } catch (ValidationException e) {
            respond lineItem.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'lineItem.label', default: 'LineItem'), lineItem.id])
                redirect lineItem
            }
            '*'{ respond lineItem, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        lineItemService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'lineItem.label', default: 'LineItem'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'lineItem.label', default: 'LineItem'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
