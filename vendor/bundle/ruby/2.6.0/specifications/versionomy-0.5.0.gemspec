# -*- encoding: utf-8 -*-
# stub: versionomy 0.5.0 ruby lib

Gem::Specification.new do |s|
  s.name = "versionomy".freeze
  s.version = "0.5.0"

  s.required_rubygems_version = Gem::Requirement.new("> 1.3.1".freeze) if s.respond_to? :required_rubygems_version=
  s.require_paths = ["lib".freeze]
  s.authors = ["Daniel Azuma".freeze]
  s.date = "2016-01-07"
  s.description = "Versionomy is a generalized version number library. It provides tools to represent, manipulate, parse, and compare version numbers in the wide variety of versioning schemes in use.".freeze
  s.email = "dazuma@gmail.com".freeze
  s.extra_rdoc_files = ["History.rdoc".freeze, "README.rdoc".freeze, "Versionomy.rdoc".freeze]
  s.files = ["History.rdoc".freeze, "README.rdoc".freeze, "Versionomy.rdoc".freeze]
  s.homepage = "http://dazuma.github.com/versionomy".freeze
  s.licenses = ["BSD-3-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 1.9.3".freeze)
  s.rubygems_version = "3.0.3".freeze
  s.summary = "Versionomy is a generalized version number library.".freeze

  s.installed_by_version = "3.0.3" if s.respond_to? :installed_by_version

  if s.respond_to? :specification_version then
    s.specification_version = 4

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<blockenspiel>.freeze, ["~> 0.5"])
    else
      s.add_dependency(%q<blockenspiel>.freeze, ["~> 0.5"])
    end
  else
    s.add_dependency(%q<blockenspiel>.freeze, ["~> 0.5"])
  end
end
