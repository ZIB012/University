function convergence_test_Phi_e(TestName,nRef)
%        CONVERGENCE TEST FOR EXTRACELLULAR POTENTIAL in norms: L2,
%        semi-H1, H1, DG, Inf
%              usage:   convergence_test_Phi_e('Test3',[3 4 5 6]) 

    num_tests=length(nRef);
    for i=1:num_tests
        fprintf('Stage %d on %d \n', i, num_tests);
        [~ ,~ ,errors_e,~ ,~ ,~ ,~ ,femregion,~ ]= main2D(TestName,nRef(i));
        err_L2(i)=errors_e.E_L2;
        err_H1(i)=errors_e.E_H1;
        err_DG(i)=errors_e.E_DG;
        err_inf(i)=errors_e.E_inf;
        h(i)=femregion.h;
    end
    close all
    figure()
    
    subplot(1,4,1)
    loglog(h, err_L2, 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_L2(1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_L2(1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    loglog(h , err_L2(1)*(h/h(1)).^4, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err L^2', 'h^2', 'h^3' , 'h^4')
    
    subplot(1,4,2)
    loglog(h, err_H1, 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_H1(1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_H1(1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_H1(1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err H^1', 'h', 'h^2', 'h^3')
    
    subplot(1,4,3)
    loglog(h, err_DG, 'o-','linewidth',2)
    hold on
    loglog(h , err_DG(1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_DG(1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_DG(1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err DG', 'h', 'h^2', 'h^3')
    
    subplot(1,4,4)
    loglog(h, err_inf, 'o-','linewidth',2)
    hold on
    loglog(h , err_inf(1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_inf(1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_inf(1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err inf', 'h', 'h^2', 'h^3')
    
    title ('convergence test for extracellular potential')
    
end