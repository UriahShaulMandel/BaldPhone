workflow "shaking finger action" {
  on = "pull_request"
  resolves = ["post gif on fail"]
}

action "post gif on fail" {
  uses = "UriahShaulMandel/shaking-finger-action@master"
  secrets = ["GITHUB_TOKEN"]
}
