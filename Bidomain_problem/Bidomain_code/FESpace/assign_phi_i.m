function [A, b] = assign_phi_i (A, b, t, Data, femregion)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Function to be used in main2D.m to trasform the system in order to automatically 
% assign the first coefficient of the unknown vector u starting from the exact solution. 
% In this way, we achieve the uniqueness of the phi solutions and the well
% conditioning of the algebraic system.
%
% Federica Botta, Matteo Calafà
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



if (Data.fem(1)=='P')
    
    x = femregion.dof(1,1);
    y = femregion.dof(1,2);
    exact_coeff = eval(Data.exact_sol_i);


elseif (Data.fem(1)=='D')
    x0=femregion.dof(3,1); % bottom-left corner of the first element
    y0=femregion.dof(3,2);
    h=femregion.dof(1,1)-femregion.dof(3,1); % length of the element

    exact_coeff = 0;
    index = 1;
    [node_1D,~, node_2D, w_2D]=quadrature(Data.nqn);
    [shape_basis] = basis_legendre_dubiner(femregion.fem);
    [phi_dub, ~,~,~] = evalshape_tria_dubiner(shape_basis,node_2D, node_1D,Data.nqn,femregion.nln);



    % the first coefficient is the L2 scalar product of uh with the first
    % basis function. To the get the right coefficient, we compute scalar product
    % between the exact solution and the first basis function
    for k = 1:length(w_2D) 
       x = x0 + h*node_2D(k,1);  %physical coordinates of the integration point
       y = y0 + h*node_2D(k,2);
       exact_coeff = exact_coeff + eval(Data.exact_sol_i)*phi_dub(1,k,index).*w_2D(k);
    end

end



% we change the system coefficients in order to impose u(1)=exact_coeff
Nh = length(b);
b = b - A(:,1)*exact_coeff;
b(1) = exact_coeff;  
A(:,1) = zeros(Nh,1);
A(1,:) = zeros(1,Nh);
A(1,1) = 1;

   