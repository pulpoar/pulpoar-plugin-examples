import UIKit
import SwiftUI
import WebKit


class PulpoARView: UIViewController, WKScriptMessageHandler, WKNavigationDelegate {

    var props: PulpoARProps
    
    private var webView: WKWebView!
    private var activityIndicator: UIActivityIndicatorView!
    private var iframeLoaded = false
    
    init(props: PulpoARProps) {
        self.props = props
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupWebView()
        setupActivityIndicator()
        setupButton()
        loadWebView()
    }
    
    private func setupWebView() {
        let contentController = WKUserContentController()
        contentController.add(self, name: "eventHandler")
        
        let config = WKWebViewConfiguration()
        config.userContentController = contentController
        config.allowsAirPlayForMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        
        webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = self
        webView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(webView)
        
        NSLayoutConstraint.activate([
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
    }
    private func setupButton() {
        let button = UIButton(type: .system)
        button.setTitle("Set Path", for: .normal)
        button.backgroundColor = .blue
        button.setTitleColor(.white, for: .normal)
        button.layer.cornerRadius = 8
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(buttonTapped), for: .touchUpInside)
        
        view.addSubview(button)
        
        NSLayoutConstraint.activate([
            button.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            button.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            button.widthAnchor.constraint(equalToConstant: 200),
            button.heightAnchor.constraint(equalToConstant: 50)
        ])
    }
    
    @objc func buttonTapped() {
        let sdk = PulpoARSDK(webView: self.webView)
        sdk.setPath("choose-model")
    }
    
    private func setupActivityIndicator() {
        activityIndicator = UIActivityIndicatorView(style: .large)
        activityIndicator.translatesAutoresizingMaskIntoConstraints = false
        activityIndicator.hidesWhenStopped = true
        view.addSubview(activityIndicator)
        
        NSLayoutConstraint.activate([
            activityIndicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            activityIndicator.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }
    
    private func loadWebView() {
        let urlString = "https://plugin.pulpoar.com/\(props.plugin)/\(props.slug)"
        guard let url = URL(string: urlString) else { return }
        
        let request = URLRequest(url: url)
        webView.load(request)
        activityIndicator.startAnimating()
    }
    
    private func injectEvents() -> String {
        return getEventScript(events: props.events)
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "eventHandler",
           let body = (message.body as? [String: Any]),
           let event = body["event"] as? String,
           let data = body["data"],
           isEventType(event) {
                let handler = getEventHandler(events: props.events)
                handler[event]!(data)
        }
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        iframeLoaded = true
        activityIndicator.stopAnimating()
        webView.evaluateJavaScript(injectEvents(), completionHandler: nil)
        
    }
    
}
