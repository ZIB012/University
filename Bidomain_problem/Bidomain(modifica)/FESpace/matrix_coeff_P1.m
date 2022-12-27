function [coef]= matrix_coeff_P1(nln)
%% [coef]= matrix_coeff_P1(nln)
% PURPOSE:
%
% This routine <omputes the coefficients of the P1 Lagrange shape 
% functions.
%     
% INPUT:
%       nln : (int) local dofs
% OUTPUT:
%       corff : (array real) coefficient of the linear expansion
%--------------------------------------------------------------------

f=zeros(nln,1);
coef_matrix=zeros(nln,nln);


%       x        y      c 
M=[
        0       0       1      % (0,0)
        1       0       1      % (1,0)
        0       1       1      % (0,1)
];

for i=1:nln
    f=zeros(nln,1);
    f(i)=1;
    coef(:,i)=M\f;
end