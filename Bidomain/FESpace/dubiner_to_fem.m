function [u0] = dubiner_to_fem (uh, femregion, Data)

%FUNCTION TO CONVERT THE COMPONENTS OF A VECTOR W.R.T. DUBINER BASIS TO COMPONONENTS W.R.T. FEM BASIS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Function to convert the components of a vector with respect to the Dubiner
%basis to components with respect to FEM basis
%Input: uh (components wrt Dubiner basis), femregion and Data
%Output: u0 (components wrt FEM basis)
%
% Federica Botta, Matteo Calafà
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

         
deg=sscanf(Data.fem(2:end),'%f');
s=0;

% define the coordinates of the degrees of freedom in the reference triangle
% and square
if (deg==1)  %D1
    a   = [-1; 1; -1];
    b   = [-1; -1; 1];
    
elseif (deg ==2) %D2
    a   = [-1; 0; 1; 1; -1; -1];
    b   = [-1; -1; -1; 0; 1; 0];

elseif (deg==3) %D3
    a   = [-1; -0.5; 0.5; 1; 1; 1; -1; -1; -1; 0];
    b   = [-1; -1; -1; -1; -0.5; 0.5; 1; 0.5; -0.5; -1/3];   
end

csi=(1+a).*(1-b)/4;
eta=(1+b)/2;

% evaluate the Dubiner basis on these points
for j=0:(deg)
    for i=0:(deg)
        if (i+j) <= deg
           s=s+1;
           [pi] = eval_jacobi_polynomial(i,0,0,a);
           [pj] = eval_jacobi_polynomial(j,2.*i+1,0,b);
           cij=sqrt((2.*i +1).*2.*(i+j+1)./4.^i);
           phi(1,:,s)=cij.*(2.^i).*((1-eta).^i).*pi.*pj;
        end
    end
end

u0 = zeros(femregion.ndof,1);

for ie = 1:femregion.ne
    
   index = (ie-1)*femregion.nln*ones(femregion.nln,1) + [1:femregion.nln]';
   
   for i = 1 : femregion.nln
       for j = 1: femregion.nln
         u0(index(i)) = u0(index(i)) +  uh(index(j))*phi(1,i,j);
       end
   end
    
end
