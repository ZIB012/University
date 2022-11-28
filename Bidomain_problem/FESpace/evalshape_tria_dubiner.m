function [dphiq,Grad,B_edge,G_edge]=evalshape_tria_dubiner(shape_basis,node_2D, node_1D,nqn,nln)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Evaluation of the Dubiner basis (dphiq) and its gradient (Grad) on the volume terms and on
%the edges (B_edge and G_edge)
%Input: shape_basis (the structure of the Dubiner basis), node_1D and
%node_2D (quadrature nodes), nqn (number of quadrature nodes in 1D) and nln
%(local degree of freedom)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

n_edge=shape_basis(end).n_edge;
nqn_2D= length(node_2D);
deg=shape_basis.deg;

% Volume terms
s=0;
for j=0:(deg)
    for i=0:(deg)
        if (i+j) <= deg
           s=s+1;
            csi=node_2D(:,1);
            eta=node_2D(:,2);
            a=2.*csi./(1-eta) - ones(nqn_2D,1);
            b=2.*eta - ones(nqn_2D,1);
            [pi] = eval_jacobi_polynomial(i,0,0,a);
            [pj] = eval_jacobi_polynomial(j,2.*i+1,0,b);
            cij=sqrt((2.*i +1).*2.*(i+j+1)./4.^i);
            
            % evaluation of the function basis
            dphiq(1,:,s)=cij.*(2.^i).*((1-eta).^i).*pi.*pj;
            
            % gradient of function basis 
            if (i==0 && j==0)
                Grad(:,1,s)=zeros(nqn_2D,1);
                Grad(:,2,s)=zeros(nqn_2D,1);
            elseif (i==0 && j~=0)
                Grad(:,1,s)=zeros(nqn_2D,1);
                Grad(:,2,s)=cij.*(j+2).*eval_jacobi_polynomial(j-1,2,1,b);
            elseif (i~=0 && j==0)
                Grad(:,1,s)=cij.*2.^i.*(1-eta).^(i-1).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a);
                Grad(:,2,s)=cij.*2.^i.*(-i.*(1-eta).^(i-1).*pi +csi.*(1-eta).^(i-2).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a));
            else
                Grad(:,1,s)=cij.*2.^(i).*(1-eta).^(i-1).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a).*eval_jacobi_polynomial(j,2.*i+1,0,b);
                Grad(:,2,s)=cij.*2.^i.*(-i.*(1-eta).^(i-1).*pi.*pj...
                    +csi.*(1-eta).^(i-2).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a).*pj...
                    +(1-eta).^(i).*(2.*i+j+2).*pi.*eval_jacobi_polynomial(j-1,2.*i+2,1,b));
            end
        end
    end
end

%=========================================================
% Evaluation on the edges
%=========================================================
B_edge=zeros(nln,nqn,n_edge);
G_edge=zeros(nqn,2,nln,n_edge);

s=0;
for j=0:(deg)
    for i=0:(deg)
        if (i+j) <= deg
             s=s+1;
            for edge=1:3
                if edge==1  % edge 1
                    csi=node_1D;
                    eta=-zeros(1, nqn);
                elseif edge==2 % edge 2
                    eta=node_1D;
                    csi=ones(1, nqn)-eta;
                elseif edge==3 % edge 3
                    csi=zeros(1, nqn);
                    eta=ones(1, nqn)-node_1D;
                end
                
                a=2.*csi./(1-eta) - ones(1,nqn);
                b=2.*eta - ones(1,nqn);
                
                [pi] = eval_jacobi_polynomial(i,0,0,a);
                [pj] = eval_jacobi_polynomial(j,2.*i+1,0,b);
                cij=sqrt((2.*i +1).*2.*(i+j+1)./4.^i);
                
                % evaluation of function basis
                B_edge(s,:,edge)=cij.*2.^i.*(1-eta).^i.*pi.*pj;
                
                % gradient of the function basis 
                if (i==0 && j==0)
                    G_edge(:,1,s,edge)=zeros(nqn,1);
                    G_edge(:,2,s,edge)=zeros(nqn,1);
                elseif (i==0 && j~=0)
                    G_edge(:,1,s,edge)=zeros(nqn,1);
                    G_edge(:,2,s,edge)=cij.*(j+2).*eval_jacobi_polynomial(j-1,2,1,b);
                elseif (i~=0 && j==0)
                    G_edge(:,1,s,edge)=cij.*2.^i.*(1-eta).^(i-1).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a);
                    G_edge(:,2,s,edge)=cij.*2.^i.*(-i.*(1-eta).^(i-1).*pi +csi.*(1-eta).^(i-2).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a));
                else
                    G_edge(:,1,s,edge)=cij.*2.^(i).*(1-eta).^(i-1).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a).*eval_jacobi_polynomial(j,2.*i+1,0,b);
                    G_edge(:,2,s,edge)=cij.*2.^i.*(-i.*(1-eta).^(i-1).*pi.*pj...
                        +csi.*(1-eta).^(i-2).*(i+1).*eval_jacobi_polynomial(i-1,1,1,a).*pj...
                        +(1-eta).^(i).*(2.*i+j+2).*pi.*eval_jacobi_polynomial(j-1,2.*i+2,1,b));
                end
            end
        end
    end
end
