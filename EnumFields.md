I got the below error when I am accessing three ENUM fields on the Form.xhtml page.

```
java.lang.IllegalArgumentException: Expected a child component type of UISelectItem/UISelectItems for component type javax.faces.SelectOne(LeadDeliveryBO_deliveryType).  Found null.
	at com.sun.faces.renderkit.RenderKitUtils.getSelectItems(RenderKitUtils.java:357)
	at com.sun.faces.renderkit.html_basic.MenuRenderer.getOptionNumber(MenuRenderer.java:675)
	at com.sun.faces.renderkit.html_basic.MenuRenderer.renderSelect(MenuRenderer.java:793)
	at com.sun.faces.renderkit.html_basic.MenuRenderer.encodeEnd(MenuRenderer.java:335)
	at javax.faces.component.UIComponentBase.encodeEnd(UIComponentBase.java:833)
	at org.ajax4jsf.renderkit.RendererBase.renderChild(RendererBase.java:286)
	at org.ajax4jsf.renderkit.RendererBase.renderChildren(RendererBase.java:262)
	at org.ajax4jsf.renderkit.html.AjaxOutputPanelRenderer.encodeChildren(AjaxOutputPanelRenderer.java:79)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.encodeRecursive(HtmlBasicRenderer.java:271)
	at com.sun.faces.renderkit.html_basic.GridRenderer.encodeChildren(GridRenderer.java:242)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.encodeRecursive(HtmlBasicRenderer.java:271)
	at com.sun.faces.renderkit.html_basic.GridRenderer.encodeChildren(GridRenderer.java:242)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at org.ajax4jsf.renderkit.RendererBase.renderChild(RendererBase.java:282)
	at org.ajax4jsf.renderkit.RendererBase.renderChildren(RendererBase.java:262)
	at org.ajax4jsf.renderkit.RendererBase.renderChild(RendererBase.java:284)
	at org.ajax4jsf.renderkit.RendererBase.renderChildren(RendererBase.java:262)
	at org.ajax4jsf.renderkit.AjaxContainerRenderer.encodeChildren(AjaxContainerRenderer.java:97)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at org.ajax4jsf.component.UIAjaxRegion.encodeChildren(UIAjaxRegion.java:113)
	at com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.encodeRecursive(HtmlBasicRenderer.java:271)
	at com.sun.faces.renderkit.html_basic.GridRenderer.encodeChildren(GridRenderer.java:242)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at org.ajax4jsf.renderkit.RendererBase.renderChild(RendererBase.java:282)
	at org.ajax4jsf.renderkit.RendererBase.renderChildren(RendererBase.java:262)
	at org.ajax4jsf.renderkit.AjaxContainerRenderer.encodeChildren(AjaxContainerRenderer.java:97)
	at javax.faces.component.UIComponentBase.encodeChildren(UIComponentBase.java:809)
	at org.ajax4jsf.component.UIAjaxRegion.encodeChildren(UIAjaxRegion.java:113)
	at javax.faces.component.UIComponent.encodeAll(UIComponent.java:886)
	at javax.faces.component.UIComponent.encodeAll(UIComponent.java:892)
	at com.sun.facelets.FaceletViewHandler.renderView(FaceletViewHandler.java:571)
	at org.ajax4jsf.application.ViewHandlerWrapper.renderView(ViewHandlerWrapper.java:108)
	at org.ajax4jsf.application.AjaxViewHandler.renderView(AjaxViewHandler.java:216)
	at com.sun.faces.lifecycle.RenderResponsePhase.execute(RenderResponsePhase.java:106)
	at com.sun.faces.lifecycle.LifecycleImpl.phase(LifecycleImpl.java:251)
	at com.sun.faces.lifecycle.LifecycleImpl.render(LifecycleImpl.java:144)
	at javax.faces.webapp.FacesServlet.service(FacesServlet.java:245)
	at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:487)
	at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1093)
	at org.apache.myfaces.webapp.filter.ExtensionsFilter.doFilter(ExtensionsFilter.java:100)
	at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1084)
	at org.ajax4jsf.webapp.BaseXMLFilter.doXmlFilter(BaseXMLFilter.java:141)
	at org.ajax4jsf.webapp.BaseFilter.doFilter(BaseFilter.java:281)
	at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1084)
	at org.apache.myfaces.webapp.filter.ExtensionsFilter.doFilter(ExtensionsFilter.java:147)
	at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1084)
	at org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter.doFilterInternal(OpenEntityManagerInViewFilter.java:111)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:75)
	at org.mortbay.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1084)
	at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:360)
	at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)
	at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:181)
	at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:712)
	at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:405)
	at org.mortbay.jetty.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:211)
	at org.mortbay.jetty.handler.HandlerCollection.handle(HandlerCollection.java:114)
	at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:139)
	at org.mortbay.jetty.Server.handle(Server.java:313)
	at org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:506)
	at org.mortbay.jetty.HttpConnection$RequestHandler.content(HttpConnection.java:844)
	at org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:644)
	at org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:211)
	at org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:381)
	at org.mortbay.io.nio.SelectChannelEndPoint.run(SelectChannelEndPoint.java:396)
	at org.mortbay.thread.BoundedThreadPool$PoolThread.run(BoundedThreadPool.java:442)
```

This is what I did with Rick's help. Needed to add a separate selectItemGenerator for each  ENUM field.
```
@SuppressWarnings("unchecked")
    @Bean (scope = DefaultScopes.SINGLETON)
    public Map<String, SelectItemGenerator> selectItemGenerators() throws Exception {
        Map<String, SelectItemGenerator> selectItemGenerators =   super.selectItemGenerators();

        SelectItemGenerator selectItemGenerator = new SelectItemGenerator();
        EnumDataSource<DeliveryMethodEnum> dataSource = new EnumDataSource();
        dataSource.setType(DeliveryMethodEnum.class);
        selectItemGenerator.setDataSource( dataSource );
        selectItemGenerators.put("DeliveryMethodEnum", selectItemGenerator);

        selectItemGenerator = new SelectItemGenerator();
        EnumDataSource<DeliveryTypeEnum> dataSource2 = new EnumDataSource();
        dataSource.setType(DeliveryTypeEnum.class);
        selectItemGenerator.setDataSource( dataSource2 );
        selectItemGenerators.put("DeliveryTypeEnum", selectItemGenerator);
        
        selectItemGenerator = new SelectItemGenerator();
        EnumDataSource<BatchModeEnum> dataSource3 = new EnumDataSource();
        dataSource.setType(BatchModeEnum.class);
        selectItemGenerator.setDataSource( dataSource2 );
        selectItemGenerators.put("BatchModeEnum", selectItemGenerator);
        
        return selectItemGenerators;
    }

```