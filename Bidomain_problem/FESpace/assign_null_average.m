function [A, b] = assign_null_average (A, b, Data, femregion)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Function to be used in main2D.m to trasform the system in order to automatically 
% impose a zero average to phi_i 
% In this way, we achieve the uniqueness of the phi solutions and the well
% conditioning of the algebraic system.
%
% Federica Botta, Matteo Calafà
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

ll = length(b)/2;
[node_1D,~, node_2D, w_2D]=quadrature(Data.nqn);

if (Data.fem(1)=='P')
    
    [shape_basis] = basis_lagrange(femregion.fem);
    [phi_fem, ~, ~, ~] = evalshape(shape_basis,node_2D,node_1D,femregion.nln);
    
    coeff = 0;
    
    for k = 1:length(w_2D)
         coeff = coeff + phi_fem(1,k,1).*w_2D(k);
    end
    
    for i = 1:ll
        A(i,2*ll+1)=coeff;
        A(2*ll+1,i)=coeff;
    end


elseif (Data.fem(1)=='D')
    
    [shape_basis] = basis_legendre_dubiner(femregion.fem);
    [phi_dub, ~,~,~] = evalshape_tria_dubiner(shape_basis,node_2D, node_1D,Data.nqn,femregion.nln);
    
    coeff =zeros(femregion.nln,1);
    
    for p = 1:femregion.nln
        p_int = 0;
        for k = 1:length(w_2D)
           p_int = p_int + phi_dub(1,k,p).*w_2D(k);
        end
        coeff(p)=p_int;
    end
    
    for i = 1:femregion.nln:ll
        A(2*ll+1,i:i+femregion.nln-1)=coeff';
        A(i:i+femregion.nln-1,2*ll+1)=coeff;
    end
    
end

A=(A+A')/2;
b = [b;0];

   