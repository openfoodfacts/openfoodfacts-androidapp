module Fastlane
  module Actions
    class SentrySetCommitsAction < Action
      def self.run(params)
        require 'shellwords'

        Helper::SentryHelper.check_sentry_cli!
        Helper::SentryConfig.parse_api_params(params)

        version = params[:version]
        version = "#{params[:app_identifier]}@#{params[:version]}" if params[:app_identifier]
        version = "#{version}+#{params[:build]}" if params[:build]

        command = [
          "sentry-cli",
          "releases",
          "set-commits",
          version
        ]

        command.push('--auto') if params[:auto]
        command.push('--clear') if params[:clear]
        command.push('--commit').push(params[:commit]) unless params[:commit].nil?

        Helper::SentryHelper.call_sentry_cli(command)
        UI.success("Successfully set commits for release: #{version}")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Set commits of a release"
      end

      def self.details
        [
          "This action allows you to set commits in a release for a project on Sentry.",
          "See https://docs.sentry.io/cli/releases/#sentry-cli-commit-integration for more information."
        ].join(" ")
      end

      def self.available_options
        Helper::SentryConfig.common_api_config_items + [
          FastlaneCore::ConfigItem.new(key: :version,
                                       description: "Release version on Sentry"),
          FastlaneCore::ConfigItem.new(key: :app_identifier,
                                      short_option: "-a",
                                      env_name: "SENTRY_APP_IDENTIFIER",
                                      description: "App Bundle Identifier, prepended to version",
                                      optional: true),
          FastlaneCore::ConfigItem.new(key: :build,
                                      short_option: "-b",
                                      description: "Release build on Sentry",
                                      optional: true),
          FastlaneCore::ConfigItem.new(key: :auto,
                                      description: "Enable completely automated commit management",
                                      is_string: false,
                                      default_value: false),
          FastlaneCore::ConfigItem.new(key: :clear,
                                      description: "Clear all current commits from the release",
                                      is_string: false,
                                      default_value: false),
          FastlaneCore::ConfigItem.new(key: :commit,
                                      description: "Commit spec, see `sentry-cli releases help set-commits` for more information",
                                      optional: true)
        ]
      end

      def self.return_value
        nil
      end

      def self.authors
        ["brownoxford"]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
