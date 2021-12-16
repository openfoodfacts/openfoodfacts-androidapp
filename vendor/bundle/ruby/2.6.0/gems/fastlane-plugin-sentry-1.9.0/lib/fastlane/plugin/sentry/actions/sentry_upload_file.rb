module Fastlane
  module Actions
    class SentryUploadFileAction < Action
      def self.run(params)
        require 'shellwords'

        Helper::SentryHelper.check_sentry_cli!
        Helper::SentryConfig.parse_api_params(params)

        version = params[:version]
        version = "#{params[:app_identifier]}@#{params[:version]}" if params[:app_identifier]
        version = "#{version}+#{params[:build]}" if params[:build]

        file = params[:file]

        command = [
          "sentry-cli",
          "releases",
          "files",
          version,
          "upload",
          file
        ]
        command.push(params[:file_url]) unless params[:file_url].nil?
        command.push("--dist").push(params[:dist]) unless params[:dist].nil?

        Helper::SentryHelper.call_sentry_cli(command)
        UI.success("Successfully uploaded files to release: #{version}")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Upload files to a release of a project on Sentry"
      end

      def self.details
        [
          "This action allows you to upload files to a release of a project on Sentry.",
          "See https://docs.sentry.io/learn/cli/releases/#upload-files for more information."
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
          FastlaneCore::ConfigItem.new(key: :dist,
                                       description: "Distribution in release",
                                       optional: true),
          FastlaneCore::ConfigItem.new(key: :file,
                                       description: "Path to the file to upload",
                                       verify_block: proc do |value|
                                         UI.user_error! "Could not find file at path '#{value}'" unless File.exist?(value)
                                       end),
          FastlaneCore::ConfigItem.new(key: :file_url,
                                       description: "Optional URL we should associate with the file",
                                       optional: true)
        ]
      end

      def self.return_value
        nil
      end

      def self.authors
        ["wschurman"]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
