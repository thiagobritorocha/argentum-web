package br.com.caelum.argentum.bean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import junit.framework.Assert;

@RunWith(Arquillian.class)
public class FiltraNegociacoesIT {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("ddMMyyyy");
	private static final String HTTP_LOCALHOST_8888 = "http://10.117.176.144:8888";
	private WebDriver driver;
	
	protected ChromeOptions options = new ChromeOptions();

	@Deployment
	public static WebArchive createWar() {

		MavenDependencyResolver resolver = DependencyResolvers.use(
				MavenDependencyResolver.class).loadMetadataFromPom("pom.xml");

		WebArchive webArchive = ShrinkWrap
				.create(WebArchive.class, "ROOT.war")
				.addPackages(true, "br.com.caelum.argentum")
				.addAsLibraries(
						resolver.artifact("com.sun.faces:jsf-api")
								.artifact("com.sun.faces:jsf-impl")
								.artifact("org.primefaces:primefaces")
								.artifact("com.thoughtworks.xstream:xstream")
								.resolveAs(GenericArchive.class))
				.as(ExplodedImporter.class)
				.importDirectory(new File("src/main/webapp"))
				.as(WebArchive.class);

		return webArchive;
	}

	@Before
	public void iniciaNavegador() {
		iniciaPhantomJs();		
	}

	@After
	public void desligaNavegador() {
		driver.quit();
	}

	@Test
	public void verificaResultadosFiltradosNaTabela()
			throws InterruptedException {
		driver.navigate().to(HTTP_LOCALHOST_8888 + "/index.xhtml");

		List<WebElement> linhasDaTabelaDeNotificacoes = driver.findElements(By
				.xpath("//*[@id='tabelaNegociacoes']/div[2]/table/tbody/tr"));

		int totalDeNotificacoesAntesDoFiltro = linhasDaTabelaDeNotificacoes
				.size();

		WebElement filtroDataDe = driver.findElement(By.name("filtroDataDe"));
		WebElement filtroDataAte = driver.findElement(By.name("filtroDataAte"));

		filtroDataDe.click(); // especialmente para o inputMask
		filtroDataDe.sendKeys(SDF.format(hojeMenos10Dias()));

		filtroDataAte.click(); // especialmente para o inputMask
		filtroDataAte.sendKeys(SDF.format(hojeMais10Dias()));

		filtroDataAte.submit();

		WebElement botaoFiltro = driver.findElement(By.name("botaoFiltro"));

		botaoFiltro.click();

		Thread.sleep(2000L);

		linhasDaTabelaDeNotificacoes = driver.findElements(By
				.xpath("//*[@id='tabelaNegociacoes']/div[2]/table/tbody/tr"));

		int totalDeNotificacoesDepoisDoFiltroAplicado = linhasDaTabelaDeNotificacoes
				.size();

		Assert.assertTrue(totalDeNotificacoesDepoisDoFiltroAplicado < totalDeNotificacoesAntesDoFiltro);
	}

	private Date hojeMais10Dias() {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, 10);
		return data.getTime();
	}

	private Date hojeMenos10Dias() {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, -10);
		return data.getTime();
	}

//	private void iniciaChrome() {
//		driver = new ChromeDriver();
//	}
	
	public void iniciaChrome(){
		System.setProperty("webdriver.chrome.driver", "C:/Drivers/chromedriver/chromedriver.exe"); 
		options.setBinary("C:\\Jenkins\\google\\chrome\\application\\chrome.exe");
		driver = new ChromeDriver(options);
	}

	private void iniciaPhantomJs() {
		
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true); // enabled by default
		caps.setCapability(
		    PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
		    "C:\\Drivers\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe"
		);
		
		caps.setJavascriptEnabled(true);
		driver = new PhantomJSDriver(caps);
	}
	
}
