package ticketModel

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class ServiceRequestController {

    static scaffold = ServiceRequest

    ServiceRequestService serviceRequestService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond serviceRequestService.list(params), model:[serviceRequestCount: serviceRequestService.count()]
    }

    def show(Long id) {
        respond serviceRequestService.get(id)
    }

    def create() {
        respond new ServiceRequest(params)
    }

    def save(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            notFound()
            return
        }

        try {
            serviceRequestService.save(serviceRequest)
        } catch (ValidationException e) {
            respond serviceRequest.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'serviceRequest.label', default: 'ServiceRequest'), serviceRequest.id])
                redirect serviceRequest
            }
            '*' { respond serviceRequest, [status: CREATED] }
        }
    }

    def edit(Long id) {
        respond serviceRequestService.get(id)
    }

    def update(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            notFound()
            return
        }

        try {
            serviceRequestService.save(serviceRequest)
        } catch (ValidationException e) {
            respond serviceRequest.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'serviceRequest.label', default: 'ServiceRequest'), serviceRequest.id])
                redirect serviceRequest
            }
            '*'{ respond serviceRequest, [status: OK] }
        }
    }

    def delete(Long id) {
        if (id == null) {
            notFound()
            return
        }

        serviceRequestService.delete(id)

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'serviceRequest.label', default: 'ServiceRequest'), id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'serviceRequest.label', default: 'ServiceRequest'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
