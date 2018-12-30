package ClientJavaFile;
public class Calculator{
	public String getFuncionNames() {return "add,sub,mul,div,pow";}
	public double add(double a,double b) {return a+b;}
	public double sub(double a,double b) {return a-b;}
	public double mul(double a,double b) {return a*b;}
	public double div(double a,double b) {return a/b;}
	public double pow(double a,int b) {
		if (b ==0) return 1;
		if (b > 1) return a*pow(a,b-1);
		if (b < 0) return 1/a * pow(a,b+1);
		return a;
	}
}