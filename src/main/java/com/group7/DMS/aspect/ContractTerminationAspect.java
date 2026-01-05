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
public class ContractTerminationAspect {

    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;

    public ContractTerminationAspect(ContractRepository contractRepository,
                                     StudentRepository studentRepository) {
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Bắt mọi lần gọi ContractService.update(contract)
     * Nếu status chuyển từ != TERMINATED -> TERMINATED
     * thì set student.registrationStatus = PENDING
     */
    @Around("execution(* com.group7.DMS.service.ContractService.update(..)) && args(contract)")
    public Object afterTerminateSetStudentPending(ProceedingJoinPoint pjp, Contracts contract) throws Throwable {

        // Lấy trạng thái trước khi update (để biết có "chuyển sang" TERMINATED không)
        Contracts.ContractStatus oldStatus = null;
        if (contract != null && contract.getId() != 0) {
            oldStatus = contractRepository.findById(contract.getId())
                    .map(Contracts::getStatus)
                    .orElse(null);
        }

        Object result = pjp.proceed();

        if (result instanceof Contracts saved) {
            // chỉ làm khi thực sự chuyển sang TERMINATED
            if (oldStatus != Contracts.ContractStatus.TERMINATED
                    && saved.getStatus() == Contracts.ContractStatus.TERMINATED
                    && saved.getStudent() != null) {

                int studentId = saved.getStudent().getId();

                studentRepository.findById(studentId).ifPresent(st -> {
                    st.setRegistrationStatus(Students.RegistrationStatus.PENDING);
                    studentRepository.save(st);
                });
            }
        }

        return result;
    }
}
