function [errors,errors_i,errors_e,errors_w,solutions,solutions_i,solutions_e,femregion,Data]= main2D_bidominio(TestName,nRef)
%==========================================================================
% Solution of the Bidomain problem with DG finite elements
% (non homogeneous Neumann boundary conditions)
%==========================================================================
%
%    INPUT:
%          Data        : (struct)  see dati.m
%          nRef        : (int)     refinement level
%
%    OUTPUT:
%          errors, errors_i, errors_e,errors_w: (struct) contains the
%          computed errors of respectively the transmembrane, intracellular,
%          extracellular potentials and gating variable potentials.
%          solutions, solutions_i, solutions_e   : (sparse) nodal values of the computed and exact
%                        solution of respectively the transmembrane,
%                        intracellular and extracellular potentials.
%          femregion   : (struct) infos about finite elements
%                        discretization
%          Data        : (struct)  see dati.m
%          
% Usage: 
%    [errors,errors_i,errors_e,errors_w,solutions,solutions_i,solutions_e,femregion,Data]= main2D_bidominio('Test1',5);


tic
addpath Assembly
addpath Errors
addpath MeshGeneration
addpath FESpace
addpath PostProcessing

close all

%==========================================================================
% LOAD DATA FOR TEST CASE
%==========================================================================

Data = dati(TestName);

%==========================================================================
% MESH GENERATION
%==========================================================================

[region] = generate_mesh(Data,nRef);

%==========================================================================
% FINITE ELEMENT REGION
%==========================================================================

[femregion] = create_dof(Data,region);


%==========================================================================
% CONNECTIVITY FOR NEIGHBOURING ELEMENTS
%==========================================================================

[neighbour] = neighbours(femregion);


%==========================================================================
% BUILD FINITE ELEMENT MATRICES and RIGHT-HAND SIDE
%==========================================================================
if (Data.fem(1) == 'D')
   [Matrices] = matrix2D_dubiner(femregion,neighbour,Data,0);
else
   [Matrices] = matrix2D(femregion,neighbour,Data,0);
end

%==========================================================================
% SOLVE THE LINEAR SYSTEM
%==========================================================================

Ai = Matrices.Ai;  %A_intracellulare
Ae = Matrices.Ae; %A_extracellulare
M = Matrices.M;

f0=zeros(1,length(Ai));


%
sigma_i = Data.Sigma_i;
sigma_e = Data.Sigma_e;
conv = sigma_e/sigma_i; %fattore di conversione per matrice di stiffness da A_i a A_e
%



%time integration parameters
t = 0;
T = Data.T;
dt = Data.dt;
theta = Data.theta;
epsilon = Data.epsilon;
gamma = Data.gamma;
ChiM = Data.ChiM;
Cm = Data.Cm;
b = Data.b;

x=femregion.dof(:,1);
y=femregion.dof(:,2);


w0 = eval(Data.initialw);

Vm0 = eval(Data.initialcond);

if (Data.fem(1)=='D')
   Vm0 = fem_to_dubiner (Vm0, femregion,Data);
   w0 = fem_to_dubiner(w0,femregion,Data);
end


ll=length(Vm0);

if (Data.method == 'SI')
   
    MASS = [M -M; -M M];
    ZERO=sparse(length(M), length(M));
    MASS_W = [M ZERO; ZERO -M];
    STIFFNESS = [Ai ZERO; ZERO  Ae];
    
    for t=dt:dt:T
        
        w1 = 1/(1+epsilon*gamma*dt)*(w0+epsilon*dt*Vm0);
        w1=cat(1,w1, w1);
        Vm0 = cat(1,Vm0,Vm0);
    
        fi = assemble_rhs_i(femregion,neighbour,Data,t);
        fe = assemble_rhs_e(femregion,neighbour,Data,t);
        f1 = cat(1, fi, fe);
    
        [C] = assemble_nonlinear(femregion,Data,Vm0);
        NONLIN = [C -C; -C C];
   
        r = f1 + ChiM*Cm/dt * MASS_W * Vm0 - b * ChiM * MASS_W *w1;
        
        B=ChiM*Cm/dt * MASS + (STIFFNESS + NONLIN);
        
        if(Data.assign==1)
           [B, r] = assign_phi_i (B, r, t, Data, femregion);
        elseif(Data.assign==2)
           [B, r] = assign_null_average(B,r,Data,femregion);
        end
       
        u1 = B \ r;
        
        if(Data.assign==2)
           u1=u1(1:end-1);
        end
   
        if (Data.snapshot=='Y' && (mod(round(t/dt),Data.leap)==0) && (t < 0.04 || t > 0.3))
             uh = u1(1:ll) - u1(ll+1:end);
             DG_Par_Snapshot(femregion, Data, uh,t);
        end
        
        f0 = f1;
        Vm0 = u1(1:ll)-u1(ll+1:end);
        u0 = u1;
        w0 = w1(1:ll);
    end

elseif (Data.method == 'OS')
    
        ZERO = sparse(ll,ll);
        
    for t=dt:dt:T
        
        [C] = assemble_nonlinear(femregion,Data,Vm0);
         Q  = (ChiM*Cm/dt)*M + C + b*(epsilon*ChiM*dt)/(1+epsilon*gamma*dt)*M;
         R  = (ChiM*Cm/dt)*M*Vm0 - b*(ChiM)/(1+epsilon*gamma*dt)*M*w0;
    
        fi = assemble_rhs_i(femregion,neighbour,Data,t);
        fe = assemble_rhs_e(femregion,neighbour,Data,t);
        f1 = cat(1, fi, -fe);
    
        B = [Q, -Q; Q, -Q] + [Ai, ZERO; ZERO, -Ae];
        r = [R;R] + f1;
        
        if(Data.assign==1)
           [B, r] = assign_phi_i (B, r, t, Data, femregion);
        elseif(Data.assign==2)
           [B, r] = assign_null_average(B,r,Data,femregion);
        end
        
        u1 = B \ r; 
        
        if(Data.assign==2)
           u1=u1(1:end-1);
        end
        
        Vm1 = u1(1:ll)-u1(ll+1:end);

        w1 = (w0 + epsilon*dt*Vm1)/(1+epsilon*gamma*dt);
    
        if (Data.snapshot=='Y' && (mod(round(t/dt),Data.leap)==0))
            uh = u1(1:ll) - u1(ll+1:end);
            DG_Par_Snapshot(femregion, Data, uh,t);
        end
        f0 = f1;
        Vm0 = u1(1:ll) - u1(ll+1:end);
        u0 = u1;
        w0 = w1;
    end

    
elseif (Data.method == 'GO')
    
    ZERO = sparse(ll,ll);
    MASS = (ChiM*Cm/dt)*[M, -M; M -M];
    MASSW = ChiM*[M, ZERO; ZERO, M];
    
    for t=dt:dt:T
        
        fi = assemble_rhs_i(femregion,neighbour,Data,t);
        fe = assemble_rhs_e(femregion,neighbour,Data,t);
        f1 = cat(1, fi, -fe);
    
        [C] = assemble_nonlinear(femregion,Data,Vm0);
        
        w1 = (1 -epsilon*gamma*dt)*w0 + epsilon*dt*Vm0;
        B = MASS + [Ai, ZERO; ZERO, -Ae];
        
        r = -b*MASSW*[w0;w0] + ((Cm/dt)*MASSW - [C, ZERO; ZERO, C])*[Vm0;Vm0] + f1;
        
        if(Data.assign==1)
           [B, r] = assign_phi_i (B, r, t, Data, femregion);
        elseif(Data.assign==2)
           [B, r] = assign_null_average(B,r,Data,femregion);
        end
        
        u1 = B \ r; 
        
        if(Data.assign==2)
           u1=u1(1:end-1);
        end
    
        if (Data.snapshot=='Y' && (mod(round(t/dt),Data.leap)==0))
            uh = u1(1:ll) - u1(ll+1:end);
            DG_Par_Snapshot(femregion, Data, uh,t);
        end
        Vm0 = u1(1:ll) - u1(ll+1:end);
        u0 = u1;
        w0 = w1;
    end
end

u0_i=u0(1:ll);
u0_e=u0(ll+1:end);

if (Data.fem(1)=='D')
   u0_i = dubiner_to_fem (u0_i,femregion,Data);
   u0_e = dubiner_to_fem (u0_e,femregion,Data);
   w0 = dubiner_to_fem (w0,femregion,Data);
end
timer = toc;
%==========================================================================
% POST-PROCESSING OF THE SOLUTION OF POTENTIALS
%=========================================================================
 uh = u0_i-u0_e;
 [solutions]= postprocessing_Vm(femregion,Data,uh,T,timer);
 [solutions_i]= postprocessing_Phi_i(femregion,Data,u0_i,T);
 [solutions_e]= postprocessing_Phi_e(femregion,Data,u0_e,T);

%==========================================================================
% ERROR ANALYSIS OF POTENTIALS
%==========================================================================
if (Data.fem(1) == 'D')
    S = matrix_S(append("P", femregion.fem(2)),femregion,neighbour,Data,0);
    [errors] = compute_errors_Vm(Data,femregion,solutions,S,T); 
    [errors_i]= compute_errors_Phi_i(Data,femregion,solutions_i,S,T);
    [errors_e]= compute_errors_Phi_e(Data,femregion,solutions_e,S,T);
else
    [errors] = compute_errors_Vm(Data,femregion,solutions,Matrices.S,T);
    [errors_i]= compute_errors_Phi_i(Data,femregion,solutions_i,Matrices.S,T);
    [errors_e]= compute_errors_Phi_e(Data,femregion,solutions_e,Matrices.S,T);
end

%==========================================================================
% SOLUTION AND ERROR ANALYSIS OF GATING VARIABLE
%==========================================================================
 
solutionsW = struct('u_h',w0,'u_ex',eval(Data.exact_w));

if (Data.fem(1) == 'D')
    [errors_w] = compute_errors_w(Data,femregion,solutionsW,S,T);
else
    [errors_w] = compute_errors_w(Data,femregion,solutionsW,Matrices.S,T);
end
end
