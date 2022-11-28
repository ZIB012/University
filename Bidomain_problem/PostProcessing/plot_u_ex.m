%%
%plot soluzione esatta p1

k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof(k:k+2,1),femregion.dof(k:k+2,2),full(solutions.u_ex(k:k+2)))
        hold on;
        k=k+3;
    end
%%
%plot soluzione esatta p2 

k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof([k,k+2,k+4],1),femregion.dof([k,k+2,k+4],2),full(solutions.u_ex([k,k+2,k+4])))
        hold on;
        k=k+6;
    end 
    
%%
    k = 1;
    for ie = 1 : femregion.ne
        trisurf([1 2 3],femregion.dof([k,k+3,k+6],1),femregion.dof([k,k+3,k+6],2),full(solutions.u_ex([k,k+3,k+6])))
        hold on;
        k=k+10;
    end
    
 %%
 
[X,Y] = meshgrid(0:.01:1); 

for T= [0:0.01:1]
Z = sin(2*pi.*X).*sin(2*pi.*Y).*exp(-5*T);
surf(X,Y,Z)
end
 %%
 close all
 clear all
[X,Y] = meshgrid(0:.01:1); 
T=0.001;
Z = 2*sin(2*pi.*X).*sin(2*pi.*Y).*exp(-5*T);
surf(X,Y,Z)
%zlim([-2 1.5])

max=2*exp(-5*T);
min=-2*exp(-5*T);
%%


[x,y] = meshgrid(0:.01:1); 
T=0.001;
Z = (x-x.^2).*(y-y.^2).*exp(-T);
surf(x,y,Z)