package com.group7.DMS.aspect;

import com.group7.DMS.entity.Contracts;
import com.group7.DMS.entity.Students;
import com.group7.DMS.repository.ContractRepository;
import com.group7.DMS.repository.StudentRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ContractReactivationAspect {

    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;

    public ContractReactivationAspect(ContractRepository contractRepository,
                                      StudentRepository studentRepository) {
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Khi contract chuyển từ TERMINATED -> ACTIVE
     * => set student.registrationStatus = APPROVED
     */
    @Around("execution(* com.group7.DMS.service.ContractService.update(..)) && args(contract)")
    public Object whenTerminatedToActiveSetStudentApproved(ProceedingJoinPoint pjp, Contracts contract) throws Throwable {

        Contracts.ContractStatus oldStatus = null;
        if (contract != null && contract.getId() != 0) {
            oldStatus = contractRepository.findById(contract.getId())
                    .map(Contracts::getStatus)
                    .orElse(null);
        }

        Object result = pjp.proceed();

        if (result instanceof Contracts saved) {
            if (oldStatus == Contracts.ContractStatus.TERMINATED
                    && saved.getStatus() == Contracts.ContractStatus.ACTIVE
                    && saved.getStudent() != null) {

                int studentId = saved.getStudent().getId();
                studentRepository.findById(studentId).ifPresent(st -> {
                    st.setRegistrationStatus(Students.RegistrationStatus.APPROVED);
                    studentRepository.save(st);
                });
            }
        }

        return result;
    }
}
