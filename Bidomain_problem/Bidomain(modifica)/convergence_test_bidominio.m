function convergence_test_bidominio(TestName,nRef)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Function for the convergence test
% of the uknowns Vm, Phi_i, Phi_e and w
%
% Federica Botta, Matteo Calafà
% convergence_test_bidominio('Test3',[3 4 5 6]);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    num_tests=length(nRef);
    err_L2 = zeros(4,num_tests);
    err_H1 = zeros(4,num_tests);
    err_DG = zeros(4,num_tests);
    err_DG = zeros(4,num_tests);
    data = dati(TestName);
    b = data.b;
    
    for i=1:num_tests
        fprintf('Stage %d on %d \n', i, num_tests);
        [errors,errors_i,errors_e,errors_w,~ ,~ ,~ ,femregion,~ ]= main2D_bidominio(TestName,nRef(i));
        err_L2(1,i)=errors.E_L2;
        err_H1(1,i)=errors.E_H1;
        err_DG(1,i)=errors.E_DG;
        err_inf(1,i)=errors.E_inf;
        err_L2(2,i)=errors_i.E_L2;
        err_H1(2,i)=errors_i.E_H1;
        err_DG(2,i)=errors_i.E_DG;
        err_inf(2,i)=errors_i.E_inf;
        err_L2(3,i)=errors_e.E_L2;
        err_H1(3,i)=errors_e.E_H1;
        err_DG(3,i)=errors_e.E_DG;
        err_inf(3,i)=errors_e.E_inf;
        err_L2(4,i)=errors_w.E_L2;
        err_H1(4,i)=errors_w.E_H1;
        err_DG(4,i)=errors_w.E_DG;
        err_inf(4,i)=errors_w.E_inf;
        h(i)=femregion.h;
    end
    close all
    figure()
    
    subplot(1,4,1)
    loglog(h, err_L2(1,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_L2(1,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_L2(1,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    loglog(h , err_L2(1,1)*(h/h(1)).^4, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err L^2', 'h^2', 'h^3' , 'h^4','Location', 'southeast')
    
    subplot(1,4,2)
    loglog(h, err_H1(1,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_H1(1,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_H1(1,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_H1(1,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err H^1', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,3)
    loglog(h, err_DG(1,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_DG(1,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_DG(1,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_DG(1,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err DG', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,4)
    loglog(h, err_inf(1,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_inf(1,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_inf(1,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_inf(1,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err inf', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    title1 = ['V_m for alpha = ', num2str(b)];
    sgtitle(title1)

    



figure()
    
    subplot(1,4,1)
    loglog(h, err_L2(2,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_L2(2,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_L2(2,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    loglog(h , err_L2(2,1)*(h/h(1)).^4, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err L^2', 'h^2', 'h^3' , 'h^4','Location', 'southeast')
    
    subplot(1,4,2)
    loglog(h, err_H1(2,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_H1(2,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_H1(2,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_H1(2,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err H^1', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,3)
    loglog(h, err_DG(2,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_DG(2,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_DG(2,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_DG(2,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err DG', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,4)
    loglog(h, err_inf(2,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_inf(2,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_inf(2,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_inf(2,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err inf', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    title2 = ['\phi_i for alpha = ', num2str(b)];
    sgtitle(title2)
    
    
    
    
figure()
    
    subplot(1,4,1)
    loglog(h, err_L2(3,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_L2(3,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_L2(3,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    loglog(h , err_L2(3,1)*(h/h(1)).^4, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err L^2', 'h^2', 'h^3' , 'h^4','Location', 'southeast')
    
    subplot(1,4,2)
    loglog(h, err_H1(3,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_H1(3,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_H1(3,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_H1(3,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err H^1', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,3)
    loglog(h, err_DG(3,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_DG(3,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_DG(3,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_DG(3,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err DG', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,4)
    loglog(h, err_inf(3,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_inf(3,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_inf(3,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_inf(3,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err inf', 'h', 'h^2', 'h^3','Location', 'southeast')    

    title3 = ['\phi_e for alpha = ', num2str(b)];
    sgtitle(title3)


    
figure()
    
    subplot(1,4,1)
    loglog(h, err_L2(4,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_L2(4,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_L2(4,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    loglog(h , err_L2(4,1)*(h/h(1)).^4, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err L^2', 'h^2', 'h^3' , 'h^4','Location', 'southeast')
    
    subplot(1,4,2)
    loglog(h, err_H1(4,:), 'o-', 'linewidth', 2)
    hold on
    loglog(h , err_H1(4,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_H1(4,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_H1(4,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err H^1', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,3)
    loglog(h, err_DG(4,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_DG(4,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_DG(4,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_DG(4,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err DG', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    subplot(1,4,4)
    loglog(h, err_inf(4,:), 'o-','linewidth',2)
    hold on
    loglog(h , err_inf(4,1)*h/h(1), 'k-', 'linewidth', 2)
    loglog(h , err_inf(4,1)*(h/h(1)).^2, 'k--', 'linewidth', 2)
    loglog(h , err_inf(4,1)*(h/h(1)).^3, 'k-.', 'linewidth', 2)
    xlabel('h')
    legend('err inf', 'h', 'h^2', 'h^3','Location', 'southeast')
    
    title4 = ['\omega for alpha = ', num2str(b)];
    sgtitle(title4)
    
end